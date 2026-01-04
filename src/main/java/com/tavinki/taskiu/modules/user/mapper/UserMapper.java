package com.tavinki.taskiu.modules.user.mapper;

import com.tavinki.taskiu.common.enums.LoginType; // 假設你的 LoginType 在這裡
import com.tavinki.taskiu.common.enums.RoleType;
import com.tavinki.taskiu.modules.auth.dto.interfaces.OAuth2UserInfo;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;
import com.tavinki.taskiu.modules.user.entity.User;

public class UserMapper {

        private UserMapper() {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        // 通用的轉 Entity 方法
        public static User toEntity(OAuth2UserInfo userInfo, LoginType loginType) {
                // build base User entity
                User.UserBuilder userBuilder = User.builder()
                                .email(userInfo.getEmail())
                                .name(userInfo.getName())
                                .picture(userInfo.getPicture())
                                .role(RoleType.USER);

                // acording to LoginType build Auth entity
                User.Auth.AuthBuilder authBuilder = User.Auth.builder();

                switch (loginType) {
                        case GOOGLE:
                                authBuilder.google(User.GoogleAuth.builder()
                                                .id(userInfo.getSub())
                                                .email(userInfo.getEmail())
                                                .build());
                                break;
                        case GITHUB:
                                authBuilder.github(User.GithubAuth.builder()
                                                .id(userInfo.getSub())
                                                .email(userInfo.getEmail())
                                                .build());
                                break;
                        default:
                                throw new IllegalArgumentException("Unsupported login type: " + loginType);
                }

                return userBuilder.auth(authBuilder.build()).build();
        }

        // 通用的更新 Auth 方法 (處理舊用戶綁定新登入方式)
        public static void updateAuthInfo(User user, OAuth2UserInfo userInfo, LoginType loginType) {
                if (user.getAuth() == null) {
                        user.setAuth(User.Auth.builder().build());
                }

                switch (loginType) {
                        case GOOGLE:
                                if (user.getAuth().getGoogle() == null) {
                                        user.getAuth().setGoogle(User.GoogleAuth.builder()
                                                        .id(userInfo.getSub())
                                                        .email(userInfo.getEmail())
                                                        .build());
                                }
                                break;
                        case GITHUB:
                                if (user.getAuth().getGithub() == null) {
                                        user.getAuth().setGithub(User.GithubAuth.builder()
                                                        .id(userInfo.getSub())
                                                        .email(userInfo.getEmail())
                                                        .build());
                                }
                                break;
                }
        }

        public static UserResponseDto toResponseDto(User user) {
                if (user == null)
                        return null;
                return UserResponseDto.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .name(user.getName())
                                .picture(user.getPicture())
                                .role(user.getRole())
                                .build();
        }
}
