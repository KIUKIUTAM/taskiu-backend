package com.tavinki.taskiu.modules.auth.dto;

import com.tavinki.taskiu.common.enums.RoleType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder // 使用 Builder 模式方便建立物件
public class UserResponseDto {
    private String id;
    private String email;
    private String name;
    private String picture;
    private RoleType role;
    // 這裡只放你想給前端看的欄位
    // 絕對不要放 password, refreshToken 等敏感欄位
}