package com.tavinki.taskiu.auth.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.tavinki.taskiu.auth.dto.GoogleUser;
import com.tavinki.taskiu.auth.mapper.UserMapper;
import com.tavinki.taskiu.auth.service.AuthService;
import com.tavinki.taskiu.mongo.entity.User;
import com.tavinki.taskiu.mongo.service.UserService;
import com.tavinki.taskiu.utils.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    private final AuthService authService;

    private final JwtUtils jwtUtils;

    @PostMapping("/google")
    public Map<String, Object> googleLogin(@RequestBody GoogleLoginRequest request, HttpServletResponse response) {

        GoogleUser userInfo = authService.authorizationCodeExchange(request.getCode(), request.getCodeVerifier());
        User existingUser = userService.getUserByEmail(userInfo.getEmail());
        String jwtToken = null;
        if (existingUser != null) {
            logger.info("Existing user logged in: {}", existingUser.getEmail());
            jwtToken = jwtUtils.generateToken(existingUser.getId(), existingUser.getName(),
                    existingUser.getEmail(),
                    existingUser.getAvatar(), existingUser.getRole());
        } else {
            logger.info("New user registration: {}", userInfo.getEmail());
            User newUser = UserMapper.googleToEntity(userInfo);
            userService.createUser(newUser);
            jwtToken = jwtUtils.generateToken(newUser.getId(), newUser.getName(), newUser.getEmail(),
                    newUser.getAvatar(), newUser.getRole());
        }

        Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
        jwtCookie.setHttpOnly(true); // Makes the cookie inaccessible to JavaScript
        jwtCookie.setSecure(true); // Ensures the cookie is only sent over HTTPS
        jwtCookie.setPath("/"); // Makes the cookie available to the entire application
        jwtCookie.setMaxAge(7 * 24 * 60 * 60); // Cookie expiration time in seconds (7 days)
        response.addCookie(jwtCookie);

        return Map.of(
                "message", "Login Successful",
                "jwtToken", jwtToken);
    }

    @Data
    public static class GoogleLoginRequest {
        private String code;
        private String codeVerifier;
    }
}
