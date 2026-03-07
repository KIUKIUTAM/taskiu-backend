package com.tavinki.taskiu.common.exceptions;

public class EmailExistsAtRegistrationException extends RuntimeException {

    public EmailExistsAtRegistrationException(String email) {
        super("Email already exists: " + email);
    }

        @Override
    public synchronized Throwable fillInStackTrace() {
        return this; 
    }
}
