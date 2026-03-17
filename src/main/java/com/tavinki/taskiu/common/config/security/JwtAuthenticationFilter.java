package com.tavinki.taskiu.common.config.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tavinki.taskiu.common.utils.JwtUtils;
import com.tavinki.taskiu.modules.auth.service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Filter
 *
 * <p>
 * This filter intercepts every HTTP request and checks the Authorization Header.
 * The process is as follows:
 * <ol>
 * <li>Check if the Header contains a Token starting with "Bearer ".</li>
 * <li>Parse the Token and verify its validity.</li>
 * <li>If verification is successful, encapsulate {@link CustomUserDetails} and store it in {@link SecurityContextHolder}.</li>
 * </ol>
 *
 * <h3>How to get the current logged-in user (Usage):</h3>
 * In the Controller, use the {@code @AuthenticationPrincipal} annotation to directly get the user object:
 *
 * <pre>
 * &#64;GetMapping("/me")
 * public ResponseEntity&lt;CustomUserDetails&gt; getCurrentUser(
 *         &#64;AuthenticationPrincipal CustomUserDetails user) {
 *     return ResponseEntity.ok(UserMapper.toResponseDto(user));
 * }
 * </pre>
 *
 * @see AuthService#extractUserFromToken(String)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

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

                Optional<CustomUserDetails> userOptional = jwtUtils.extractUserFromToken(jwt);

                if (userOptional.isPresent()) {
                    CustomUserDetails user = userOptional.get();

                    List<SimpleGrantedAuthority> authorities = (user.getRole() == null)
                            ? Collections.emptyList()
                            : Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("JWT Authentication successful for user: {}", user.getId());

                } else {
                    log.warn("JWT Authentication failed for token: {}", jwt);
                }
            }
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
