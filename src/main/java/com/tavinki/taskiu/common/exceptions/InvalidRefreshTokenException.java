package com.tavinki.taskiu.common.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }


    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; 
    }
}
