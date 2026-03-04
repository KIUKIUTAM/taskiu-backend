package com.tavinki.taskiu.modules.user.controller; // Recommended to place in user package


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tavinki.taskiu.modules.user.dto.UserResponseDto;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {


    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @AuthenticationPrincipal UserResponseDto user) {
        log.info("Fetching current user info for user: {}", user != null ? user.getEmail() : "Anonymous");
        return ResponseEntity.ok(user);
    }
}
