package com.tavinki.taskiu.common.exceptions.minio;

public class MinioDownloadException extends MinioException {
    public MinioDownloadException(String filename, Throwable cause) {
        super("downloadFailed:" + filename, cause);
    }
}