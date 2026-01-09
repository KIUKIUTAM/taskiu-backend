package com.tavinki.taskiu.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tavinki.taskiu.modules.auth.dto.interfaces.OAuth2UserInfo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
// DTO representing a Google user
public class GoogleUser implements OAuth2UserInfo {

    private String email;
    private String name;
    private String sub;
    private String picture;

}
