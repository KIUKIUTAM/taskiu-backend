package com.tavinki.taskiu.common.exceptions.minio;

public class MinioUploadException extends MinioException {
    public MinioUploadException(String filename) {
        super("Failed to upload file: " + filename);
    }

    public MinioUploadException(String filename, Throwable cause) {
        super("Failed to upload file: " + filename, cause);
    }
}