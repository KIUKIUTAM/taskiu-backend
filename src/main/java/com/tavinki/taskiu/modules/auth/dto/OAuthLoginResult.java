package com.tavinki.taskiu.modules.auth.dto;

import com.tavinki.taskiu.modules.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthLoginResult {

    private final User user;
    private final String avatarUploadError;

    public boolean hasAvatarError() {
        return avatarUploadError != null;
    }

}
