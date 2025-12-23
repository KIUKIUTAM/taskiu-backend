package com.tavinki.taskiu.modules.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tavinki.taskiu.modules.auth.dto.UserResponseDto;
import com.tavinki.taskiu.modules.auth.service.AuthService;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;

    private static final Logger customLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. 取得 Header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 2. 如果沒有 Header 或格式不對，直接放行 (交給後面的 SecurityConfig 決定是否攔截)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // 3. 驗證 Token
        try {
            // 如果 SecurityContext 已經有驗證資訊，就不重複驗證
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                Optional<UserResponseDto> userOptional = authService.extractUserFromToken(jwt);

                if (userOptional.isPresent()) {
                    UserResponseDto user = userOptional.get();

                    // 4. 建立 Authentication 物件 (這是 Spring Security 認得的憑證)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.emptyList());

                    // 5. 將驗證資訊放入 SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    customLogger.info("JWT Authentication successful for user: {}", user.getId());

                } else {
                    customLogger.warn(jwt);
                }
            }
        } catch (Exception e) {
            // Token 驗證失敗，這裡可以選擇記錄 Log，但不建議拋出異常，讓它繼續走，Security 會因為沒登入而擋下
            customLogger.error("JWT Authentication failed: {}", e.getMessage());
        }

        // 6. 繼續執行下一個 Filter
        filterChain.doFilter(request, response);
    }
}