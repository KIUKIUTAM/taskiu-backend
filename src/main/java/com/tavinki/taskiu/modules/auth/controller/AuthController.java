package com.tavinki.taskiu.modules.auth.controller;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

import com.tavinki.taskiu.common.enums.LoginType;
import com.tavinki.taskiu.common.exception.InvalidRefreshTokenException;
import com.tavinki.taskiu.common.utils.HttpUtils;
import com.tavinki.taskiu.modules.auth.dto.GoogleUser;
import com.tavinki.taskiu.modules.auth.dto.interfaces.OAuth2UserInfo;
import com.tavinki.taskiu.modules.auth.dto.GitHubUser;
import com.tavinki.taskiu.modules.auth.dto.GoogleOrGithubLoginRequest;
import com.tavinki.taskiu.modules.auth.service.AuthService;
import com.tavinki.taskiu.modules.auth.service.RefreshTokenService;
import com.tavinki.taskiu.modules.auth.service.RefreshTokenService.RefreshTokenResult;
import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.modules.user.mapper.UserMapper;
import com.tavinki.taskiu.modules.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger customLogger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    private final AuthService authService;

    private final RefreshTokenService refreshTokenService;

    private static final String MESSAGE = "message";

    private static final String ACCESS_TOKEN = "accessToken";

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody GoogleOrGithubLoginRequest googleRequest,
            HttpServletRequest request) {

        LoginType loginType = LoginType.GOOGLE;
        String ipAddress = HttpUtils.getRequestIP(request);
        String userAgent = HttpUtils.getUserAgent(request);

        GoogleUser userInfo = authService.googleAuthorizationCodeExchange(googleRequest.getCode(),
                googleRequest.getCodeVerifier());
        User user = userService.getUserByEmail(userInfo.getEmail());
        String accessToken = null;
        String refreshToken = null;
        if (user != null) {
            customLogger.info("Existing user logged in: {}", user.getEmail());
        } else {
            customLogger.info("New user registration: {}", userInfo.getEmail());
            user = UserMapper.toEntity(userInfo, LoginType.GOOGLE);
            user = userService.createUser(user);
        }
        accessToken = authService.generateJwtToken(user);
        refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookieFrom(refreshToken).toString())
                .body(Map.of(
                        MESSAGE, "Login Successful",
                        ACCESS_TOKEN, accessToken,
                        "login_type", LoginType.GOOGLE));
    }

    @PostMapping("/github")
    public ResponseEntity<Map<String, Object>> githubLogin(@RequestBody GoogleOrGithubLoginRequest githubRequest,
            HttpServletRequest request) {

        String ipAddress = HttpUtils.getRequestIP(request);
        String userAgent = HttpUtils.getUserAgent(request);

        GitHubUser userInfo = authService.githubAuthorizationCodeExchange(githubRequest.getCode(),
                githubRequest.getCodeVerifier());

        User user = null;
        if (userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
            user = userService.getUserByEmail(userInfo.getEmail());
        }
        String accessToken = null;
        String refreshToken = null;
        if (user != null) {
            customLogger.info("Existing user logged in: {}", user.getEmail());
        } else {
            customLogger.info("New user registration: {}", userInfo.getEmail());
            user = UserMapper.toEntity(userInfo, LoginType.GITHUB);
            user = userService.createUser(user);
        }
        accessToken = authService.generateJwtToken(user);
        refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookieFrom(refreshToken).toString())
                .body(Map.of(
                        MESSAGE, "Login Successful",
                        ACCESS_TOKEN, accessToken,
                        "login_type", LoginType.GOOGLE));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logoutUser(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                clearRefreshTokenCookieFrom().toString())
                .body(Map.of(MESSAGE, "Logout successful"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletRequest request) {
        ResponseCookie cookie = null;
        String ipAddress = HttpUtils.getRequestIP(request);
        String userAgent = HttpUtils.getUserAgent(request);

        if (refreshToken == null) {
            throw new InvalidRefreshTokenException("Refresh token is missing");
        }

        RefreshTokenResult result = refreshTokenService.processRefreshToken(refreshToken, ipAddress,
                userAgent);

        if (!result.isValid()) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }
        User user = userService.getUserById(result.userId());

        String newAccessToken = authService.generateJwtToken(user);

        if (result.isRotated()) {
            cookie = generateRefreshTokenCookieFrom(result.token());
        }

        if (cookie != null) {
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                    cookie.toString()).body(new TokenResponse(newAccessToken));
        }

        return ResponseEntity.ok().body(new TokenResponse(newAccessToken));
    }

    public record TokenResponse(
            String accessToken) {
    }

    public ResponseCookie generateRefreshTokenCookieFrom(String validRefreshToken) {
        return ResponseCookie.from("refreshToken", validRefreshToken)
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(7L * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie clearRefreshTokenCookieFrom() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(0L)
                .sameSite("Lax")
                .build();
    }

    private User getOrCreateUser(OAuth2UserInfo userInfo, LoginType loginType) {

        User user = userService.getUserByEmail(userInfo.getEmail());

        if (user != null) {
            customLogger.info("Existing user logged in: {}", user.getEmail());

            UserMapper.updateAuthInfo(user, userInfo, loginType);

            userService.updateUser(user);

        } else {
            customLogger.info("New user registration via {}: {}", loginType, userInfo.getEmail());

            user = UserMapper.toEntity(userInfo, loginType);
            user = userService.createUser(user);
        }

        return user;
    }

}
