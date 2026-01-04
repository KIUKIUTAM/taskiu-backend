package com.tavinki.taskiu.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleOrGithubLoginRequest {
    private String code;

    @JsonProperty("code_verifier")
    private String codeVerifier;
}