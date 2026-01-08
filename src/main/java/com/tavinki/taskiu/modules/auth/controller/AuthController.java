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
import com.tavinki.taskiu.modules.auth.dto.EmailLoginRequest;
import com.tavinki.taskiu.modules.auth.dto.EmailRegisterRequest;
import com.tavinki.taskiu.modules.auth.dto.GitHubUser;
import com.tavinki.taskiu.modules.auth.dto.GoogleOrGithubLoginRequest;
import com.tavinki.taskiu.modules.auth.service.AuthService;
import com.tavinki.taskiu.modules.auth.service.RefreshTokenService;
import com.tavinki.taskiu.modules.auth.service.RefreshTokenService.RefreshTokenResult;
import com.tavinki.taskiu.modules.turnstile.service.TurnstileService;
import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.modules.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Objects;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

        private static final Logger customLogger = LoggerFactory.getLogger(AuthController.class);

        private final UserService userService;

        private final AuthService authService;

        private final RefreshTokenService refreshTokenService;

        private final TurnstileService turnstileService;

        private static final String MESSAGE = "message";

        private static final String LOGIN_TYPE = "login_type";

        private static final String ACCESS_TOKEN = "accessToken";

        private static final String LOGIN_SUCCESS = "Login Successful";

        @PostMapping("/google")
        public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody GoogleOrGithubLoginRequest googleRequest,
                        HttpServletRequest request) {

                String ipAddress = HttpUtils.getRequestIP(request);
                String userAgent = HttpUtils.getUserAgent(request);
                GoogleUser userInfo = authService.googleAuthorizationCodeExchange(googleRequest.getCode(),
                                googleRequest.getCodeVerifier());
                User user = authService.getOrCreateUserForOAuth(userInfo, LoginType.GOOGLE);
                String accessToken = authService.generateJwtToken(user);
                String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);
                customLogger.info("Google login successful for user: {}, IP: {}", user.getEmail(),
                                ipAddress);
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookieForm(refreshToken).toString())
                                .body(Map.of(
                                                MESSAGE, LOGIN_SUCCESS,
                                                ACCESS_TOKEN, accessToken,
                                                LOGIN_TYPE, LoginType.GOOGLE));
        }

        @PostMapping("/github")
        public ResponseEntity<Map<String, Object>> githubLogin(@RequestBody GoogleOrGithubLoginRequest githubRequest,
                        HttpServletRequest request) {

                String ipAddress = HttpUtils.getRequestIP(request);
                String userAgent = HttpUtils.getUserAgent(request);

                GitHubUser userInfo = authService.githubAuthorizationCodeExchange(githubRequest.getCode(),
                                githubRequest.getCodeVerifier());

                User user = authService.getOrCreateUserForOAuth(userInfo, LoginType.GITHUB);
                String accessToken = authService.generateJwtToken(user);
                String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);
                customLogger.info("GitHub login successful for user: {}, IP: {}", user.getEmail(),
                                ipAddress);
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookieForm(refreshToken).toString())
                                .body(Map.of(
                                                MESSAGE, LOGIN_SUCCESS,
                                                ACCESS_TOKEN, accessToken,
                                                LOGIN_TYPE, LoginType.GITHUB));
        }

        @PostMapping("login")
        public ResponseEntity<Map<String, Object>> emailLogin(@RequestBody EmailLoginRequest loginRequest,
                        HttpServletRequest request) {
                String ipAddress = HttpUtils.getRequestIP(request);
                String userAgent = HttpUtils.getUserAgent(request);
                User user = authService.validateUserCredentials(loginRequest.getEmail(), loginRequest.getPassword());
                String accessToken = authService.generateJwtToken(user);
                String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);
                customLogger.info("Email login successful for user: {}, IP: {}", user.getEmail(),
                                ipAddress);
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookieForm(refreshToken).toString())
                                .body(Map.of(
                                                MESSAGE, LOGIN_SUCCESS,
                                                ACCESS_TOKEN, accessToken,
                                                LOGIN_TYPE, LoginType.EMAIL));
        }

        @PostMapping("register")
        public ResponseEntity<Map<String, Object>> emailRegister(@RequestBody EmailRegisterRequest registerRequest,
                        HttpServletRequest request) {

                // error handling is done in GlobalExceptionHandler
                String ipAddress = HttpUtils.getRequestIP(request);
                String userAgent = HttpUtils.getUserAgent(request);
                turnstileService.validateToken(ACCESS_TOKEN, ipAddress);
                User user = authService.registerNewUser(registerRequest.getEmail(), registerRequest.getPassword());
                String accessToken = authService.generateJwtToken(user);
                String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);
                customLogger.info("Email registration successful for user: {}, IP: {}", user.getEmail(),
                                ipAddress);
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, generateRefreshTokenCookieForm(refreshToken).toString())
                                .body(Map.of(MESSAGE, "Registration successful",
                                                ACCESS_TOKEN, accessToken,
                                                LOGIN_TYPE, LoginType.EMAIL));
        }

        @PostMapping("/logout")
        public ResponseEntity<Map<String, Object>> logoutUser(
                        @CookieValue(value = "refreshToken", required = false) String refreshToken) {
                if (refreshToken != null) {
                        customLogger.info("Logout attempt with refresh token: {}", refreshToken);
                        refreshTokenService.deleteRefreshToken(refreshToken);
                }
                customLogger.info("Logout successful for refresh token: {}", refreshToken);
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                                clearRefreshTokenCookieForm().toString())
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

                // Process the refresh token, result contains info about validity and rotation
                RefreshTokenResult result = refreshTokenService.processRefreshToken(refreshToken, ipAddress,
                                userAgent);

                if (!result.isValid()) {
                        throw new InvalidRefreshTokenException("Invalid or expired refresh token");
                }
                User user = userService.getUserById(Objects.requireNonNull(result.userId()));

                String newAccessToken = authService.generateJwtToken(user);

                // If the token was rotated, set a new refresh token cookie
                if (result.isRotated()) {
                        cookie = generateRefreshTokenCookieForm(result.token());
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

        public ResponseCookie generateRefreshTokenCookieForm(String validRefreshToken) {
                return ResponseCookie.from("refreshToken", validRefreshToken)
                                .httpOnly(false)
                                .secure(false)
                                .path("/")
                                .maxAge(7L * 24 * 60 * 60)
                                .sameSite("Lax")
                                .build();
        }

        public ResponseCookie clearRefreshTokenCookieForm() {
                return ResponseCookie.from("refreshToken", "")
                                .httpOnly(false)
                                .secure(false)
                                .path("/")
                                .maxAge(0L)
                                .sameSite("Lax")
                                .build();
        }

}
