package com.tavinki.taskiu.modules.auth.controller;

import lombok.Data;
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
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody GoogleLoginRequest googleRequest,
            HttpServletRequest request) {

        String ipAddress = HttpUtils.getRequestIP(request);
        String userAgent = HttpUtils.getUserAgent(request);

        GoogleUser userInfo = authService.authorizationCodeExchange(googleRequest.getCode(),
                googleRequest.getCode_verifier());
        User user = userService.getUserByEmail(userInfo.getEmail());
        String accessToken = null;
        String refreshToken = null;
        if (user != null) {
            customLogger.info("Existing user logged in: {}", user.getEmail());
        } else {
            customLogger.info("New user registration: {}", userInfo.getEmail());
            user = UserMapper.googleToEntity(userInfo);
            user = userService.createUser(user);
        }
        accessToken = authService.generateJwtToken(user);
        refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookie(refreshToken).toString())
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
                clearRefreshTokenCookie().toString())
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
            cookie = generateRefreshTokenCookie(result.token());
        }

        if (cookie != null) {
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                    cookie.toString()).body(new TokenResponse(newAccessToken));
        }

        return ResponseEntity.ok().body(new TokenResponse(newAccessToken));
    }

    @Data
    public static class GoogleLoginRequest {
        private String code;
        private String code_verifier;
    }

    public record TokenResponse(
            String accessToken) {
    }

    public ResponseCookie generateRefreshTokenCookie(String validRefreshToken) {
        return ResponseCookie.from("refreshToken", validRefreshToken)
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(7L * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(0L)
                .sameSite("Lax")
                .build();
    }

}
