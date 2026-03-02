package com.tavinki.taskiu.modules.user.dto;

import com.tavinki.taskiu.common.enums.role.SystemRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private String id;
    private String email;
    private String name;
    private String picture;
    private SystemRole role;
    private boolean verified;
    private boolean banned;
}