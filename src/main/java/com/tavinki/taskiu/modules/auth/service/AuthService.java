package com.tavinki.taskiu.modules.auth.service;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavinki.taskiu.common.enums.RoleType;
import com.tavinki.taskiu.common.exception.AuthException;
import com.tavinki.taskiu.common.properties.GoogleLoginProperties;
import com.tavinki.taskiu.common.utils.JwtUtils;
import com.tavinki.taskiu.modules.auth.dto.GoogleUser;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;
import com.tavinki.taskiu.modules.user.entity.User;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger customLogger = LoggerFactory.getLogger(AuthService.class);

    private final RestClient.Builder restClientBuilder;

    private final JwtUtils jwtUtils;

    private final GoogleLoginProperties googleLoginProperties;

    // Exchange authorization code for user info from Google
    public GoogleUser authorizationCodeExchange(String authorizationCode, String codeVerifier) {

        RestClient restClient = restClientBuilder.build();

        ParameterizedTypeReference<Map<String, Object>> mapType = new ParameterizedTypeReference<Map<String, Object>>() {
        };

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authorizationCode);
        formData.add("client_id", googleLoginProperties.getClientId());
        formData.add("client_secret", googleLoginProperties.getClientSecret());
        formData.add("redirect_uri", googleLoginProperties.getRedirectUri());
        formData.add("grant_type", "authorization_code");
        formData.add("code_verifier", codeVerifier);

        try {
            Map<String, Object> response = restClient.post()
                    .uri(Objects.requireNonNull(googleLoginProperties.getTokenUri()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(mapType);

            if (response == null || response.get("id_token") == null) {
                throw new AuthException("Invalid response from Google");
            }
            String idToken = (String) response.get("id_token");
            String[] chunks = idToken.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(payload, GoogleUser.class);

        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            String status = e.getStatusCode().toString();
            customLogger.error("Google Login Failed: status={}, body={}", status, body, e);
            throw new AuthException("Google login failed: " + status + " " + body, e);
        } catch (Exception e) {
            customLogger.error("Internal Server Error while handling Google login", e);
            throw new AuthException("Internal Server Error", e);
        }
    }

    public String generateJwtToken(User user) {
        return jwtUtils.generateToken(user.getId(), user.getName(), user.getEmail(), user.getPicture(), user.getRole());
    }

    // authenticate
    public Optional<UserResponseDto> extractUserFromToken(String jwt) {

        Claims claims = jwtUtils.getClaims(jwt).orElse(null);
        if (claims == null) {
            return Optional.empty();
        }
        return Optional.of(UserResponseDto.builder()
                .id(claims.getSubject())
                .name((String) claims.get("name"))
                .email((String) claims.get("email"))
                .picture((String) claims.get("picture"))
                .role(RoleType.valueOf((String) claims.get("role")))
                .build());
    }

}
