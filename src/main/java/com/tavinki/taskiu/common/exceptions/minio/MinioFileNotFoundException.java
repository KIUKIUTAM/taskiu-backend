package com.tavinki.taskiu.common.exceptions.minio;

public class MinioFileNotFoundException extends MinioException {
    public MinioFileNotFoundException(String filename) {
        super("fileNotFound:" + filename);
    }
}