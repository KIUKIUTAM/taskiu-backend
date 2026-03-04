package com.tavinki.taskiu.common.exceptions.minio;

public class MinioUploadException extends MinioException {
    public MinioUploadException(String filename, Throwable cause) {
        super("uploadFailed:" + filename, cause);
    }
}