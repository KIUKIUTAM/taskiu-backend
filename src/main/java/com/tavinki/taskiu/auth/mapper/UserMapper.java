package com.tavinki.taskiu.auth.mapper;

import com.tavinki.taskiu.auth.dto.GoogleUser;
import com.tavinki.taskiu.mongo.entity.User;

public class UserMapper {

    private UserMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static User googleToEntity(GoogleUser userInfo) {
        return User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .avatar(userInfo.getPicture())
                .auth(User.Auth.builder()
                        .google(User.GoogleAuth.builder()
                                .id(userInfo.getSub())
                                .email(userInfo.getEmail())
                                .build())
                        .build())
                .build();
    }
}
