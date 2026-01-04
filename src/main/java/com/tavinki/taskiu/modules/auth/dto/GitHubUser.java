package com.tavinki.taskiu.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tavinki.taskiu.modules.auth.dto.interfaces.OAuth2UserInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
// DTO representing a GitHub user
public class GitHubUser implements OAuth2UserInfo {

    private String email;
    private String name;

    @JsonProperty("id")
    private String sub;

    @JsonProperty("avatar_url")
    private String picture;

}
