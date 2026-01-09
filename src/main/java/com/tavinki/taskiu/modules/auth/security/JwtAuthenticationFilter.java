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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tavinki.taskiu.common.utils.JwtUtils;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * JWT 認證過濾器 (JWT Authentication Filter)
 *
 * <p>
 * 此過濾器會攔截每一個 HTTP 請求，檢查 Authorization Header。
 * 流程如下：
 * <ol>
 * <li>檢查 Header 是否包含 "Bearer " 開頭的 Token。</li>
 * <li>解析 Token 並驗證有效性。</li>
 * <li>若驗證成功，將 {@link UserResponseDto} 封裝並存入 {@link SecurityContextHolder}。</li>
 * </ol>
 *
 * <h3>如何獲取當前登入使用者 (Usage):</h3>
 * 在 Controller 中，請使用 {@code @AuthenticationPrincipal} 註解直接取得使用者物件：
 *
 * <pre>
 * &#64;GetMapping("/me")
 * public ResponseEntity&lt;UserResponseDto&gt; getCurrentUser(
 *         &#64;AuthenticationPrincipal UserResponseDto user) {
 *     return ResponseEntity.ok(user);
 * }
 * </pre>
 *
 * @see AuthService#extractUserFromToken(String)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private static final Logger customLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                Optional<UserResponseDto> userOptional = jwtUtils.extractUserFromToken(jwt);

                if (userOptional.isPresent()) {
                    UserResponseDto user = userOptional.get();

                    List<SimpleGrantedAuthority> authorities = (user.getRole() == null)
                            ? Collections.emptyList()
                            : Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    customLogger.info("JWT Authentication successful for user: {}", user.getId());

                } else {
                    customLogger.warn(jwt);
                }
            }
        } catch (Exception e) {
            customLogger.error("JWT Authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
