package com.tavinki.taskiu;

import com.tavinki.taskiu.common.properties.MinioProperties;
import com.tavinki.taskiu.modules.user.dto.AvatarUploadResult;
import com.tavinki.taskiu.modules.user.service.AvatarMinioService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class AvatarMinioServiceIntegrationTest {

    // =========================================================
    // 啟動真實 MinIO Container
    // =========================================================
    @Container
    static MinIOContainer minioContainer = new MinIOContainer("minio/minio:latest")
            .withUserName("minioadmin")
            .withPassword("minioadmin");

    static MinioClient minioClient;
    static MinioProperties minioProperties;
    static AvatarMinioService avatarMinioService;

    // Mock 外部圖片 URL（代替真實 Google OAuth URL）
    static MockWebServer mockWebServer;

    static final String BUCKET_NAME = "taskiu-test";

    // =========================================================
    // 初始化：建立 MinioClient、bucket、MockWebServer
    // =========================================================
    @BeforeAll
    static void setUp() throws Exception {
        // 建立真實 MinioClient，連接到 Testcontainers 的 MinIO
        minioClient = MinioClient.builder()
                .endpoint(minioContainer.getS3URL())
                .credentials(minioContainer.getUserName(), minioContainer.getPassword())
                .build();

        // 建立 bucket
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
        }

        // 設定 MinioProperties
        minioProperties = new MinioProperties();
        minioProperties.setBucketName(BUCKET_NAME);
        minioProperties.setPrefix(Map.of("avatar", "avatars/"));

        // 啟動 MockWebServer（模擬外部圖片 URL）
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // 建立真實 Service
        avatarMinioService = new AvatarMinioService(minioClient, minioProperties);
    }

    @AfterAll
    static void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    // =========================================================
    // uploadFromUrl - 真實上傳成功
    // =========================================================
    @Test
    void uploadFromUrl_ShouldActuallyUploadToMinio_WhenImageIsValid() throws Exception {
        // Arrange
        byte[] fakeImageBytes = "fake-image-content".getBytes();
        Buffer buffer = new Buffer().write(fakeImageBytes);

        // MockWebServer 回傳假圖片
        mockWebServer.enqueue(new MockResponse()
                .setBody(buffer)
                .addHeader("Content-Type", "image/jpeg"));

        String imageUrl = mockWebServer.url("/photo.jpg").toString();
        String userId = "user123";

        // Act
        AvatarUploadResult result = avatarMinioService.uploadFromUrl(userId, imageUrl);

        // Assert - 驗證 result
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getKey()).startsWith("avatars/");
        assertThat(result.getKey()).endsWith("user123.jpg");

        // Assert - 真的去 MinIO 查檔案存不存在
        var stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(result.getKey())
                        .build());

        assertThat(stat).isNotNull();
        assertThat(stat.object()).isEqualTo(result.getKey());
    }

    // =========================================================
    // uploadFromUrl - 圖片 URL 無效（404）
    // =========================================================
    @Test
    void uploadFromUrl_ShouldReturnFailure_WhenImageUrlReturns404() {
        // Arrange - MockWebServer 回傳 404
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        String imageUrl = mockWebServer.url("/not-found.jpg").toString();
        String userId = "user456";

        // Act
        AvatarUploadResult result = avatarMinioService.uploadFromUrl(userId, imageUrl);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Failed to upload avatar");
    }

    // =========================================================
    // uploadFromUrl - 圖片內容為空
    // =========================================================
    @Test
    void uploadFromUrl_ShouldReturnFailure_WhenImageIsEmpty() {
        // Arrange - MockWebServer 回傳空 body
        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .addHeader("Content-Type", "image/jpeg"));

        String imageUrl = mockWebServer.url("/empty.jpg").toString();
        String userId = "user789";

        // Act
        AvatarUploadResult result = avatarMinioService.uploadFromUrl(userId, imageUrl);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Avatar image is empty");
    }
}
