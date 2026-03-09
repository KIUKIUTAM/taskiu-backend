package com.tavinki.taskiu.common.exceptions.minio;

public class MinioDownloadException extends MinioException {
    public MinioDownloadException(String filename, Throwable cause) {
        super("downloadFailed:" + filename, cause);
    }
    public MinioDownloadException(String key) {
        super("Failed to download file: " + key);
    }
    
}