package com.tavinki.taskiu;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tavinki.taskiu.common.properties.MinioProperties;
import com.tavinki.taskiu.modules.user.dto.AvatarUploadResult;
import com.tavinki.taskiu.modules.user.service.AvatarMinioService;

import io.minio.MinioClient;

@ExtendWith(MockitoExtension.class)
class AvatarMinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    private AvatarMinioService avatarMinioService;

    // Mock RestClient chain
    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        avatarMinioService = spy(new AvatarMinioService(minioClient, minioProperties));
    }

    // =========================================================
    // uploadFromUrl - Success
    // =========================================================

    @Test
    void uploadFromUrl_ShouldReturnSuccess_WhenImageDownloadedAndUploaded() {
        // Arrange
        String userId = "user123";
        String imageUrl = "https://lh3.googleusercontent.com/photo.jpg";
        byte[] fakeImageBytes = "fake-image-content".getBytes();
        String expectedKey = "avatars/user123.jpg";

        try (MockedStatic<RestClient> restClientMock = mockStatic(RestClient.class)) {
            restClientMock.when(RestClient::create).thenReturn(restClient);
            doReturn(requestHeadersUriSpec).when(restClient).get();
            doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(imageUrl);
            doReturn(responseSpec).when(requestHeadersSpec).retrieve();
            doReturn(fakeImageBytes).when(responseSpec).body(byte[].class);

            doReturn(expectedKey).when(avatarMinioService).upload(anyString(), any(),anyInt(), anyString());

            // Act
            AvatarUploadResult result = avatarMinioService.uploadFromUrl(userId, imageUrl);

            // Assert
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getKey()).isEqualTo(expectedKey);
            assertThat(result.getErrorMessage()).isNull();
        }
    }

    // =========================================================
    // uploadFromUrl - Empty Image
    // =========================================================

    @Test
    void uploadFromUrl_ShouldReturnFailure_WhenImageBytesAreNull() {
        // Arrange
        String userId = "user123";
        String imageUrl = "https://lh3.googleusercontent.com/photo.jpg";

        try (MockedStatic<RestClient> restClientMock = mockStatic(RestClient.class)) {
            restClientMock.when(RestClient::create).thenReturn(restClient);
            doReturn(requestHeadersUriSpec).when(restClient).get();
            doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(imageUrl);
            doReturn(responseSpec).when(requestHeadersSpec).retrieve();
            doReturn(null).when(responseSpec).body(byte[].class);

            // Act
            AvatarUploadResult result = avatarMinioService.uploadFromUrl(userId, imageUrl);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getKey()).isNull();
            assertThat(result.getErrorMessage()).isEqualTo("Avatar image is empty");
        }
    }

    @Test
    void uploadFromUrl_ShouldReturnFailure_WhenImageBytesAreEmpty() {
        // Arrange
        String userId = "user123";
        String imageUrl = "https://lh3.googleusercontent.com/photo.jpg";

        try (MockedStatic<RestClient> restClientMock = mockStatic(RestClient.class)) {
            restClientMock.when(RestClient::create).thenReturn(restClient);
            doReturn(requestHeadersUriSpec).when(restClient).get();
            doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(imageUrl);
            doReturn(responseSpec).when(requestHeadersSpec).retrieve();
            doReturn(new byte[0]).when(responseSpec).body(byte[].class);

            // Act
            AvatarUploadResult result = avatarMinioService.uploadFromUrl(userId, imageUrl);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getKey()).isNull();
            assertThat(result.getErrorMessage()).isEqualTo("Avatar image is empty");
        }
    }

    // =========================================================
    // uploadFromUrl - Network Error
    // =========================================================

    @Test
    void uploadFromUrl_ShouldReturnFailure_WhenNetworkErrorOccurs() {
        // Arrange
        String userId = "user123";
        String imageUrl = "https://lh3.googleusercontent.com/photo.jpg";

        try (MockedStatic<RestClient> restClientMock = mockStatic(RestClient.class)) {
            restClientMock.when(RestClient::create).thenReturn(restClient);
            doReturn(requestHeadersUriSpec).when(restClient).get();
            doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(imageUrl);
            doReturn(responseSpec).when(requestHeadersSpec).retrieve();
            doThrow(new RestClientException("Network error")).when(responseSpec).body(byte[].class);

            // Act
            AvatarUploadResult result = avatarMinioService.uploadFromUrl(userId, imageUrl);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getKey()).isNull();
            assertThat(result.getErrorMessage()).isEqualTo("Failed to upload avatar");
        }
    }

    // =========================================================
    // uploadFromUrl - MinIO Upload Error
    // =========================================================

    @Test
    void uploadFromUrl_ShouldReturnFailure_WhenMinioUploadFails() {
        // Arrange
        String userId = "user123";
        String imageUrl = "https://lh3.googleusercontent.com/photo.jpg";
        byte[] fakeImageBytes = "fake-image-content".getBytes();

        try (MockedStatic<RestClient> restClientMock = mockStatic(RestClient.class)) {
            restClientMock.when(RestClient::create).thenReturn(restClient);
            doReturn(requestHeadersUriSpec).when(restClient).get();
            doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(imageUrl);
            doReturn(responseSpec).when(requestHeadersSpec).retrieve();
            doReturn(fakeImageBytes).when(responseSpec).body(byte[].class);

            doThrow(new RuntimeException("MinIO connection refused"))
                    .when(avatarMinioService).upload(anyString(), any(),anyInt(), anyString());

            // Act
            AvatarUploadResult result = avatarMinioService.uploadFromUrl(userId, imageUrl);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getKey()).isNull();
            assertThat(result.getErrorMessage()).isEqualTo("Failed to upload avatar");
        }
    }

}
