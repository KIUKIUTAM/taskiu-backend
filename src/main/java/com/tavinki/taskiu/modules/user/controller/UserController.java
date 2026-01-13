package com.tavinki.taskiu.modules.user.controller; // 建議放在 user package 下

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tavinki.taskiu.modules.user.dto.UserResponseDto;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Logger customLogger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @AuthenticationPrincipal UserResponseDto user) {
        customLogger.info("Fetching current user info for user: {}", user != null ? user.getEmail() : "Anonymous");
        return ResponseEntity.ok(user);
    }
}
