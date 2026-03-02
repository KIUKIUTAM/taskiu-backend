
package com.tavinki.taskiu.common.exceptions;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EmailVerifyException extends RuntimeException {

    public EmailVerifyException(String message) {
        super(message);
    }

    public EmailVerifyException(String message, Throwable cause) {
        super(message, cause);
    }

}
