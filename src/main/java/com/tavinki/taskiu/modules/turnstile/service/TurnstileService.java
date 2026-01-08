package com.tavinki.taskiu.modules.turnstile.service;

import org.springframework.web.client.RestTemplate;

import com.tavinki.taskiu.modules.turnstile.dto.TurnstileResponse;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TurnstileService {

    @Value("${cloudflare.turnstile.secret-key}")
    private String secretKey;

    @Value("${cloudflare.turnstile.verify-url}")
    private String verifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public TurnstileResponse validateToken(String token, String remoteip) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", secretKey);
        params.add("response", token);
        if (remoteip != null) {
            params.add("remoteip", remoteip);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<TurnstileResponse> response = restTemplate.postForEntity(
                    Objects.requireNonNull(verifyUrl), request, TurnstileResponse.class);
            return response.getBody();
        } catch (Exception e) {
            TurnstileResponse errorResponse = new TurnstileResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorCodes(List.of("internal-error"));
            return errorResponse;
        }
    }

}
