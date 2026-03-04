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
     * Helper Function to get current logged-in user
     */
    public static Optional<UserResponseDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        // Ensure principal is of expected UserResponseDto type
        if (principal instanceof UserResponseDto) {
            return Optional.of((UserResponseDto) principal);
        }

        return Optional.empty();
    }
}
