package com.tavinki.taskiu.auth.service;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavinki.taskiu.auth.dto.GoogleUser;
import com.tavinki.taskiu.exception.AuthException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final RestClient.Builder restClientBuilder;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.token-uri}")
    private String tokenUri;

    public GoogleUser authorizationCodeExchange(String authorizationCode, String codeVerifier) {

        RestClient restClient = restClientBuilder.build();

        ParameterizedTypeReference<Map<String, Object>> mapType = new ParameterizedTypeReference<Map<String, Object>>() {
        };

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authorizationCode);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");
        formData.add("code_verifier", codeVerifier);

        try {
            Map<String, Object> response = restClient.post()
                    .uri(Objects.requireNonNull(tokenUri))
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
            String body = e.getResponseBodyAsString(); // 簡化寫法
            String status = (e.getStatusCode() != null) ? e.getStatusCode().toString() : "UNKNOWN";
            logger.error("Google Login Failed: status={}, body={}", status, body, e);
            throw new AuthException("Google login failed: " + status + " " + body, e);
        } catch (Exception e) {
            logger.error("Internal Server Error while handling Google login", e);
            throw new AuthException("Internal Server Error", e);
        }
    }

}
