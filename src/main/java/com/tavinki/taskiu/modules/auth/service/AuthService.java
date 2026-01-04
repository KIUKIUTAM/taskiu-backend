package com.tavinki.taskiu.modules.auth.service;

import java.util.Base64;
import java.util.List;
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
import com.tavinki.taskiu.common.properties.GithubLoginProperties;
import com.tavinki.taskiu.common.utils.JwtUtils;
import com.tavinki.taskiu.modules.auth.dto.GoogleUser;
import com.tavinki.taskiu.modules.auth.dto.GitHubUser;
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
    private final GithubLoginProperties githubLoginProperties;

    // Exchange authorization code for user info from Google
    public GoogleUser googleAuthorizationCodeExchange(String authorizationCode, String codeVerifier) {

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
            customLogger.error("Google login failed: {} {}", status, body);
            throw new AuthException("Google login failed: " + status + " " + body, e);
        } catch (Exception e) {
            customLogger.error("Internal Server Error", e);
            throw new AuthException("Internal Server Error", e);
        }
    }

    // unfinished - Exchange authorization code for user info from GitHub
    // googleuser used for simplicity
    public GitHubUser githubAuthorizationCodeExchange(String authorizationCode, String codeVerifier) {

        RestClient restClient = restClientBuilder.build();

        // Exchange authorization code for access token
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authorizationCode);
        formData.add("client_id", githubLoginProperties.getClientId());
        formData.add("client_secret", githubLoginProperties.getClientSecret());
        formData.add("code_verifier", codeVerifier);
        formData.add("redirect_uri", githubLoginProperties.getRedirectUri());

        Map<String, Object> tokenResponse = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new AuthException("Failed to retrieve access token from GitHub");
        }

        String accessToken = (String) tokenResponse.get("access_token");
        GitHubUser userInfo = null;
        try {
            userInfo = restClient.get()
                    .uri("https://api.github.com/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(GitHubUser.class);

            // if email is null, fetch from /user/emails endpoint, which requires extra
            // scope(email)
            // Or email ends with noreply.github.com
            if (userInfo != null) {
                boolean needFetchEmail = userInfo.getEmail() == null
                        || userInfo.getEmail().isBlank()
                        || userInfo.getEmail().endsWith("noreply.github.com");

                if (needFetchEmail) {
                    // 呼叫 /user/emails
                    List<Map<String, Object>> emails = restClient.get()
                            .uri("https://api.github.com/user/emails")
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .body(new ParameterizedTypeReference<>() {
                            });

                    if (emails != null) {
                        String primaryEmail = emails.stream()
                                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                                .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                                .map(e -> (String) e.get("email"))
                                .findFirst()
                                .orElse(null); // 如果真的找不到，就保持 null

                        // 只有抓到了才覆蓋，沒抓到就維持原樣 (可能是 null 或 noreply)
                        if (primaryEmail != null) {
                            userInfo.setEmail(primaryEmail);
                        }
                    }
                }
            }
            return userInfo;
        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            String status = e.getStatusCode().toString();
            customLogger.error("GitHub login failed: {} {}", status, body);
            throw new AuthException("GitHub login failed: " + status + " " + body, e);
        } catch (Exception e) {
            customLogger.error("Failed to fetch user profile from GitHub", e);
            throw new AuthException("Failed to fetch user profile from GitHub", e);
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
