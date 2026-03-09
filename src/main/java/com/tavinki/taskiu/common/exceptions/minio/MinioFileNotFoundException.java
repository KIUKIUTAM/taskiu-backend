package com.tavinki.taskiu.common.exceptions.minio;

public class MinioFileNotFoundException extends MinioException {

    public MinioFileNotFoundException(String key) {
        super("File not found in MinIO: " + key);
    }
    public MinioFileNotFoundException(String key, Throwable cause) {
        super("File not found in MinIO: " + key, cause);
    }
    
}