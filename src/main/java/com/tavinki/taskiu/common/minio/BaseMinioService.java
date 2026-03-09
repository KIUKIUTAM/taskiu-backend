package com.tavinki.taskiu.common.minio;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.tavinki.taskiu.common.exceptions.minio.MinioDeleteException;
import com.tavinki.taskiu.common.exceptions.minio.MinioDownloadException;
import com.tavinki.taskiu.common.exceptions.minio.MinioException;
import com.tavinki.taskiu.common.exceptions.minio.MinioFileNotFoundException;
import com.tavinki.taskiu.common.exceptions.minio.MinioUploadException;
import com.tavinki.taskiu.common.properties.MinioProperties;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseMinioService {

    protected final MinioClient minioClient;
    protected final MinioProperties minioProperties;

    protected BaseMinioService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    protected abstract String getPrefix();

    protected String buildKey(String filename) {
        return getPrefix() + UUID.randomUUID() + "_" + filename;
    }

    /**
     * Generate a presigned GET URL for the given key.
     * URL is valid for 7 days.
     */
    public String getPresignedUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucketName())
                            .object(key)
                            .expiry(7, TimeUnit.DAYS)
                            .build());
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", key, e);
            // ✅ 使用 MinioException，訊息由 exception 本身管理
            throw new MinioException("Failed to generate presigned URL for key: " + key, e);
        }
    }

    /**
     * Upload a file with an auto-generated key.
     *
     * @return the generated object key
     */
    public String upload(String filename, InputStream inputStream, String contentType) {
        try {
            String key = buildKey(filename);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(key)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build());
            log.info("Uploaded file: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Failed to upload file: {}", filename, e);
            // ✅ 訊息由 MinioUploadException 統一管理
            throw new MinioUploadException(filename, e);
        }
    }

    /**
     * Upload a file with a specified key (no auto-generation).
     *
     * @return the provided object key
     */
    public String uploadWithKey(String key, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(key)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build());
            log.info("Uploaded file with key: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Failed to upload file with key: {}", key, e);
            // ✅ 同樣使用 MinioUploadException
            throw new MinioUploadException(key, e);
        }
    }

    /**
     * Download a file by its full object key.
     *
     * @return InputStream of the file content
     * @throws MinioFileNotFoundException if the object does not exist
     */
    public InputStream download(String key) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(key)
                            .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.warn("File not found: {}", key);
                // ✅ 使用 MinioFileNotFoundException，訊息由 exception 管理
                throw new MinioFileNotFoundException(key);
            }
            log.error("Failed to download file: {}", key, e);
            throw new MinioDownloadException(key, e);
        } catch (Exception e) {
            log.error("Failed to download file: {}", key, e);
            // ✅ 使用 MinioDownloadException
            throw new MinioDownloadException(key, e);
        }
    }

    /**
     * Delete a file by its full object key.
     *
     * @throws MinioDeleteException if deletion fails
     */
    public void delete(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(key)
                            .build());
            log.info("Deleted file: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", key, e);
            // ✅ 使用 MinioDeleteException
            throw new MinioDeleteException(key, e);
        }
    }
}
