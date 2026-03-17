package com.tavinki.taskiu.modules.user.mapper;

import com.tavinki.taskiu.common.config.security.CustomUserDetails;
import com.tavinki.taskiu.common.enums.LoginType;
import com.tavinki.taskiu.common.enums.role.SystemRole;
import com.tavinki.taskiu.modules.auth.dto.interfaces.OAuth2UserInfo;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;
import com.tavinki.taskiu.modules.user.entity.User;

import lombok.extern.slf4j.Slf4j;


@Slf4j
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
                        case GOOGLE ->
                                authBuilder.google(User.GoogleAuth.builder()
                                                .id(userInfo.getSub())
                                                .email(userInfo.getEmail())
                                                .build());
                        case GITHUB ->
                                authBuilder.github(User.GithubAuth.builder()
                                                .id(userInfo.getSub())
                                                .email(userInfo.getEmail())
                                                .build());
                        default ->
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
                        case GOOGLE -> {
                                if (user.getAuth().getGoogle() == null) {
                                        user.getAuth().setGoogle(User.GoogleAuth.builder()
                                                        .id(userInfo.getSub())
                                                        .email(userInfo.getEmail())
                                                        .build());
                                }else{
                                        log.info("User {} already has Google auth info, skipping update", user.getEmail());
                                }
                        }

                        case GITHUB -> {
                                if (user.getAuth().getGithub() == null) {
                                        user.getAuth().setGithub(User.GithubAuth.builder()
                                                        .id(userInfo.getSub())
                                                        .email(userInfo.getEmail())
                                                        .build());
                                }else{
                                        log.info("User {} already has Github auth info, skipping update", user.getEmail());
                                }
                        }

                        default ->
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

        public static UserResponseDto toResponseDto(CustomUserDetails userDetails ,String pictureUrl) {
                if (userDetails == null)
                        return null;
                
        return UserResponseDto.builder()
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .name(userDetails.getName())
                .picture(pictureUrl)
                .role(userDetails.getRole())
                .verified(userDetails.isVerified())
                .banned(userDetails.isBanned())
                .build();
        }


}
