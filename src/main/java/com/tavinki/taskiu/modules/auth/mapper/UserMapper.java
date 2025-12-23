package com.tavinki.taskiu.modules.auth.mapper;

import com.tavinki.taskiu.common.enums.RoleType;
import com.tavinki.taskiu.modules.auth.dto.GoogleUser;
import com.tavinki.taskiu.modules.auth.dto.UserResponseDto;
import com.tavinki.taskiu.modules.user.entity.User;

public class UserMapper {

    private UserMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static User googleToEntity(GoogleUser userInfo) {
        return User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .picture(userInfo.getPicture())
                .role(RoleType.USER)
                .auth(User.Auth.builder()
                        .google(User.GoogleAuth.builder()
                                .id(userInfo.getSub())
                                .email(userInfo.getEmail())
                                .build())
                        .build())
                .build();
    }

    public static UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .picture(user.getPicture())
                .role(user.getRole())
                .build();
    }
}
