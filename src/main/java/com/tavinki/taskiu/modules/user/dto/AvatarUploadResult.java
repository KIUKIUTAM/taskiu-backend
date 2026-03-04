package com.tavinki.taskiu.modules.user.dto;

import lombok.Getter;

@Getter
public class AvatarUploadResult {

    private final String key;
    private final boolean success;
    private final String errorMessage;

    private AvatarUploadResult(String key, boolean success, String errorMessage) {
        this.key = key;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static AvatarUploadResult success(String key) {
        return new AvatarUploadResult(key, true, null);
    }

    public static AvatarUploadResult failure(String errorMessage) {
        return new AvatarUploadResult(null, false, errorMessage);
    }

}
