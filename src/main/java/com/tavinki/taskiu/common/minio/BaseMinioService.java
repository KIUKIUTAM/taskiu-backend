package com.tavinki.taskiu.common.minio;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.tavinki.taskiu.common.exceptions.minio.MinioDeleteException;
import com.tavinki.taskiu.common.exceptions.minio.MinioDownloadException;
import com.tavinki.taskiu.common.exceptions.minio.MinioFileNotFoundException;
import com.tavinki.taskiu.common.exceptions.minio.MinioUploadException;
import com.tavinki.taskiu.common.properties.MinioProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class BaseMinioService {

    protected final MinioClient minioClient;

    protected final MinioProperties minioProperties;

    protected abstract String getPrefix();

    protected String buildKey(String filename) {
        return getPrefix() + UUID.randomUUID() + "_" + filename;
    }

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
            throw new MinioUploadException(filename, e);
        }
    }

    public InputStream download(String filename) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(buildKey(filename))
                            .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.warn("File not found: {}", filename);
                throw new MinioFileNotFoundException(filename);
            }
            log.error("Failed to download file: {}", filename, e);
            throw new MinioDownloadException(filename, e);
        } catch (Exception e) {
            log.error("Failed to download file: {}", filename, e);
            throw new MinioDownloadException(filename, e);
        }
    }

    public void delete(String filename) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(buildKey(filename))
                            .build());
            log.info("Deleted file: {}", filename);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", filename, e);
            throw new MinioDeleteException(filename, e);
        }
    }

}
