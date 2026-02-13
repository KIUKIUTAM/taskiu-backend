package com.tavinki.taskiu.modules.email.controller;

import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tavinki.taskiu.modules.auth.service.AuthService;
import com.tavinki.taskiu.modules.email.service.EmailService;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;
import com.tavinki.taskiu.modules.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailVerifyController {
    private static final String MESSAGE = "message";

    private static final String ACCESS_TOKEN = "accessToken";

    private final EmailService emailService;

    private final AuthService authService;

    @PostMapping("/send-verify-email")
    public ResponseEntity<Map<String, Object>> sendEmail(@AuthenticationPrincipal UserResponseDto user) {
        if (user == null || user.getEmail() == null) {
            log.warn("Unauthorized attempt to send verification email.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MESSAGE, "User is not authenticated"));
        }
        String toEmail = Objects.requireNonNull(user.getEmail());
        try {
            // Check rate limiting before sending email (1min per email)
            if (emailService.limitingSendEmail(toEmail)) {
                log.warn("Email sending rate limit exceeded for email: {}", toEmail);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of(MESSAGE, "Too many requests. Please try again later."));
            }
            // Send verification code to the email
            emailService.sendVerificationCode(toEmail);
            log.info("Verification code sent to email: {}", toEmail);
            return ResponseEntity.ok(Map.of(MESSAGE, "sent successfully"));
        } catch (Exception e) {
            log.error("Failed to send verification code to email: {}", toEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(MESSAGE, "sending failed"));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@AuthenticationPrincipal UserResponseDto user,
            @RequestBody VerifyRequest request) {

        if (user == null || user.getEmail() == null) {
            log.warn("Unauthorized attempt to send verification email.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MESSAGE, "User is not authenticated"));
        }
        String toEmail = Objects.requireNonNull(user.getEmail());
        // Process verification and set the email as verified if successful
        User verifiedUser = emailService.verifyProcess(toEmail, request.getCode());
        if (verifiedUser != null) {
            log.info("Email verification successful for email: {}", toEmail);
            String accessToken = authService.generateJwtToken(verifiedUser);
            return ResponseEntity.ok(Map.of(MESSAGE, "verification successful", ACCESS_TOKEN, accessToken));
        } else {
            log.warn("Email verification failed for email: {}", toEmail);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(MESSAGE, "verification code is incorrect or has expired"));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyRequest {

        @JsonProperty("verify_code")
        private String code;
    }
}
