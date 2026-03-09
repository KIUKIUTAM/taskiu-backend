package com.tavinki.taskiu.common.exceptions.minio;

public class MinioDeleteException extends MinioException {
    public MinioDeleteException(String filename, Throwable cause) {
        super("deleteFailed:" + filename, cause);
    }

    public MinioDeleteException(String key) {
        super("Failed to delete file: " + key);
    }
}