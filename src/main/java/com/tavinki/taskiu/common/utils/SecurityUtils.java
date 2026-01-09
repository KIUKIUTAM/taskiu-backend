package com.tavinki.taskiu.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;
import java.util.Optional;

public class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 獲取當前登入使用者的 Helper Function
     */
    public static Optional<UserResponseDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        // 確保 principal 是我們預期的 UserResponseDto 類型
        if (principal instanceof UserResponseDto) {
            return Optional.of((UserResponseDto) principal);
        }

        return Optional.empty();
    }
}
