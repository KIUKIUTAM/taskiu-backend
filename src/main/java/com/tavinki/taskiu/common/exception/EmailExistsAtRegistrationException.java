package com.tavinki.taskiu.common.exception;

public class EmailExistsAtRegistrationException extends RuntimeException {

    public EmailExistsAtRegistrationException(String email) {
        super("Email already exists: " + email);
    }

}
