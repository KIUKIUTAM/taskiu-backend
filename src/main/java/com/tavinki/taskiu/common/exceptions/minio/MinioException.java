package com.tavinki.taskiu.common.exceptions.minio;

public class MinioException extends RuntimeException {
    public MinioException(String message) {
        super(message);
    }

    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; 
    }
}
