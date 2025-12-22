package com.tavinki.taskiu.modules.user.controller; // 建議放在 user package 下

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tavinki.taskiu.modules.auth.dto.UserResponseDto;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @AuthenticationPrincipal UserResponseDto user) {
        return ResponseEntity.ok(user);
    }
}
