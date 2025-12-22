package com.tavinki.taskiu.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
// DTO representing a Google user
public class GoogleUser {

    private String email;
    private String name;
    private String sub;
    private String picture;

}
