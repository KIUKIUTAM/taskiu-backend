package com.tavinki.taskiu.modules.user.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tavinki.taskiu.common.exceptions.minio.MinioUploadException;
import com.tavinki.taskiu.common.minio.BaseMinioService;
import com.tavinki.taskiu.common.properties.MinioProperties;
import com.tavinki.taskiu.modules.user.dto.AvatarUploadResult;

import io.minio.MinioClient;


@Slf4j
@Service
public class AvatarMinioService extends BaseMinioService {

    public AvatarMinioService(MinioClient minioClient, MinioProperties minioProperties) {
        super(minioClient, minioProperties);
    }
    
    @Override
    protected String getPrefix() {
        return minioProperties.getPrefix().get("avatar"); // avatars/
    }

    /**
     * Download image from OAuth picture URL and upload to MinIO
     *
     * @param userId   User ID, used as filename
     * @param imageUrl  OAuth picture URL
     * @return MinIO key
     */
    public AvatarUploadResult uploadFromUrl(String userId, String imageUrl) {
    log.info("Downloading avatar from Google OAuth for user: {}", userId);

    try {
        byte[] imageBytes = RestClient.create()
                .get()
                .uri(imageUrl)
                .retrieve()
                .body(byte[].class);

        if (imageBytes == null || imageBytes.length == 0) {
            log.warn("Downloaded empty avatar for user: {}", userId);
            return AvatarUploadResult.failure("Avatar image is empty");
        }

        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            String filename = userId + ".jpg";
            String key = upload(filename, inputStream, "image/jpeg");
            log.info("Avatar uploaded to MinIO for user: {}, key: {}", userId, key);
            return AvatarUploadResult.success(key);
        }

    } catch (Exception e) {
        log.error("Failed to upload avatar for user: {}", userId, e);
        return AvatarUploadResult.failure("Failed to upload avatar");
    }
}
}
