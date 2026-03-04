package com.tavinki.taskiu.modules.user.mapper;

import com.tavinki.taskiu.common.enums.LoginType;
import com.tavinki.taskiu.common.enums.role.SystemRole;
import com.tavinki.taskiu.modules.auth.dto.interfaces.OAuth2UserInfo;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;
import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.modules.user.service.AvatarMinioService;



public class UserMapper {

        private UserMapper() {
                throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static User toEntity(OAuth2UserInfo userInfo, LoginType loginType) {

                
                // build base User entity
                User.UserBuilder userBuilder = User.builder()
                                .email(userInfo.getEmail())
                                .name(userInfo.getName())
                                .picture(userInfo.getPicture())
                                .role(SystemRole.USER);

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

        // update existing User entity with new OAuth2 info
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
                        case EMAIL:
                                // Handle other login types here if necessary
                                break;
                        default:
                                throw new IllegalArgumentException("Unsupported login type: " + loginType);
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
