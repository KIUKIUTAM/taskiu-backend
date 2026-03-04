package com.tavinki.taskiu.common.exceptions.minio;

public class MinioDeleteException extends MinioException {
    public MinioDeleteException(String filename, Throwable cause) {
        super("deleteFailed:" + filename, cause);
    }
}