package com.tavinki.taskiu.modules.auth.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;


import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tavinki.taskiu.common.enums.LoginType;
import com.tavinki.taskiu.common.exception.InvalidRefreshTokenException;
import com.tavinki.taskiu.common.properties.DeployProperties;
import com.tavinki.taskiu.common.utils.HttpUtils;
import com.tavinki.taskiu.modules.auth.dto.EmailLoginRequest;
import com.tavinki.taskiu.modules.auth.dto.GitHubUser;
import com.tavinki.taskiu.modules.auth.dto.GoogleOrGithubLoginRequest;
import com.tavinki.taskiu.modules.auth.dto.GoogleUser;
import com.tavinki.taskiu.modules.auth.dto.RegisterRequest;
import com.tavinki.taskiu.modules.auth.service.AuthService;
import com.tavinki.taskiu.modules.auth.service.RefreshTokenService;
import com.tavinki.taskiu.modules.auth.service.RefreshTokenService.RefreshTokenResult;
import com.tavinki.taskiu.modules.email.service.EmailService;
import com.tavinki.taskiu.modules.turnstile.dto.TurnstileResponse;
import com.tavinki.taskiu.modules.turnstile.service.TurnstileService;
import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.modules.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

        private final UserService userService;

        private final AuthService authService;

        private final EmailService emailService;

        private final RefreshTokenService refreshTokenService;

        private final TurnstileService turnstileService;

        private final DeployProperties deployProperties;

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
                log.info("Google login successful for user: {}, IP: {}", user.getEmail(),
                                ipAddress);
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE,
                                                refreshTokenService.generateRefreshTokenCookieForm(refreshToken)
                                                                .toString())
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
                log.info("GitHub login successful for user: {}, IP: {}", user.getEmail(),
                                ipAddress);
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE,
                                                refreshTokenService.generateRefreshTokenCookieForm(refreshToken)
                                                                .toString())
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
                log.info("Email login successful for user: {}, IP: {}", user.getEmail(),
                                ipAddress);
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE,
                                                refreshTokenService.generateRefreshTokenCookieForm(refreshToken)
                                                                .toString())
                                .body(Map.of(
                                                MESSAGE, LOGIN_SUCCESS,
                                                ACCESS_TOKEN, accessToken,
                                                LOGIN_TYPE, LoginType.EMAIL));
        }

        @PostMapping("register")
        public ResponseEntity<Map<String, Object>> emailRegister(
                        @Valid @RequestBody RegisterRequest registerRequest,
                        HttpServletRequest request) {

                String ipAddress = HttpUtils.getRequestIP(request);
                String userAgent = HttpUtils.getUserAgent(request);

                log.info("Registration attempt from IP: {}, turnstile token: {}", ipAddress,
                                registerRequest.getTurnstileToken());
                TurnstileResponse turnstileResponse = turnstileService
                                .validateToken(registerRequest.getTurnstileToken(), ipAddress);
                log.info("Turnstile response: {}", turnstileResponse);
                String expectedDomain = getDomainFromUrl(deployProperties.getExpectedHost());
                log.info("Expected domain for Turnstile verification: {}", expectedDomain);
                if (!turnstileResponse.isSuccess()
                                || !turnstileResponse.getHostname().equals(expectedDomain)) {
                        log.warn("Turnstile verification failed during registration from IP: {}, errors: {}",
                                        ipAddress,
                                        turnstileResponse.getErrorCodes());
                        return ResponseEntity.status(400).body(Map.of(MESSAGE, "Turnstile verification failed"));
                }

                String email = Objects.requireNonNull(registerRequest.getEmail());
                String rawPassword = Objects.requireNonNull(registerRequest.getPassword());
                User user = authService.registerNewUser(email, rawPassword);
                emailService.sendVerificationCode(user.getEmail());
                String accessToken = authService.generateJwtToken(user);
                String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);
                log.info("Email registration successful for user: {}, IP: {}", user.getEmail(),
                                ipAddress);
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE,
                                                refreshTokenService.generateRefreshTokenCookieForm(refreshToken)
                                                                .toString())
                                .body(Map.of(MESSAGE, "Registration successful",
                                                ACCESS_TOKEN, accessToken,
                                                LOGIN_TYPE, LoginType.EMAIL));
        }

        @PostMapping("/logout")
        public ResponseEntity<Map<String, Object>> logoutUser(
                        @CookieValue(value = "refreshToken", required = false) String refreshToken) {
                if (refreshToken != null) {
                        log.info("Logout attempt with refresh token: {}", refreshToken);
                        refreshTokenService.deleteRefreshToken(refreshToken);
                }
                log.info("Logout successful for refresh token: {}", refreshToken);
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                                refreshTokenService.clearRefreshTokenCookieForm().toString())
                                .body(Map.of(MESSAGE, "Logout successful"));
        }

        public record TokenResponse(
                        String accessToken) {
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

                if (user == null) {
                        throw new InvalidRefreshTokenException("User not found for the given refresh token");
                }
                String newAccessToken = authService.generateJwtToken(user);

                // If the token was rotated, set a new refresh token cookie
                if (result.isRotated()) {
                        cookie = refreshTokenService.generateRefreshTokenCookieForm(result.token());
                }

                if (cookie != null) {
                        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                                        cookie.toString()).body(new TokenResponse(newAccessToken));
                }

                return ResponseEntity.ok().body(new TokenResponse(newAccessToken));
        }

        private String getDomainFromUrl(String url) {
                try {
                        // 如果 url 不包含 http/https，URI 解析可能會出錯，這裡做個簡單判斷
                        if (!url.startsWith("http")) {
                                return url;
                        }
                        return new URI(url).getHost(); // 會把 https://tavinki.com 變成 tavinki.com
                } catch (URISyntaxException e) {
                        // 錯誤處理或是直接回傳原字串
                        return url;
                }
        }
}
