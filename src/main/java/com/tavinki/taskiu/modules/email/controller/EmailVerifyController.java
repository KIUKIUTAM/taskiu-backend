package com.tavinki.taskiu.modules.email.controller;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import com.tavinki.taskiu.modules.email.service.EmailService;

@RestController
@RequestMapping("/email")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class EmailVerifyController {
    private static final String MESSAGE = "message";
    private final Logger customLogger = LoggerFactory.getLogger(EmailVerifyController.class);

    private final EmailService emailService;

    @PostMapping("/sendEmail")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody @NonNull String toEmail) {
        try {
            emailService.sendVerificationCode(toEmail);
            customLogger.info("Verification code sent to email: {}", toEmail);
            return ResponseEntity.ok(Map.of(MESSAGE, "sent successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            customLogger.error("Failed to send verification code to email: {}", toEmail, e);
            return ResponseEntity.status(500).body(Map.of(MESSAGE, "sending failed"));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody @NonNull String toEmail,
            @RequestBody @NonNull String code) {

        boolean isValid = emailService.verifyCode(toEmail, code);
        if (isValid) {
            customLogger.info("Email verification successful for email: {}", toEmail);
            return ResponseEntity.ok(Map.of(MESSAGE, "verification successful"));
        } else {
            customLogger.warn("Email verification failed for email: {}", toEmail);
            return ResponseEntity.status(400).body(Map.of(MESSAGE, "verification code is incorrect or has expired"));
        }
    }
}