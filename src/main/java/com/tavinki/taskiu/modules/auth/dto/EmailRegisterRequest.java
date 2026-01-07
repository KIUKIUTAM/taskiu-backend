package com.tavinki.taskiu.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRegisterRequest {
    private String email;
    private String name;
    private String password;

}
