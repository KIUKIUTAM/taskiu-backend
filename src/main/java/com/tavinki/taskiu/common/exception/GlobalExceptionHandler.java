package com.tavinki.taskiu.common.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserExists(UserAlreadyExistsException ex) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage());

        problem.setTitle("User Already Exists");

        problem.setType(Objects.requireNonNull(URI.create("urn:taskiu:errors:user-exists")));

        problem.setProperty("timestamp", System.currentTimeMillis());

        return problem;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Object> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        ProblemDetail errorBody = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
        ResponseCookie clearCookie = clearRefreshTokenCookie();
        // 3. 回傳 401 Unauthorized 並帶上 Set-Cookie header
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(errorBody);
    }

    // todo: react the following code after implementing registration feature
    @ExceptionHandler(EmailExistsAtRegistrationException.class)
    public ProblemDetail handleEmailExists(EmailExistsAtRegistrationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage());

        problem.setTitle("Email Already Exists");

        problem.setType(Objects.requireNonNull(URI.create("urn:taskiu:errors:email-exists")));

        problem.setProperty("timestamp", System.currentTimeMillis());

        return problem;
    }

    // 4. 攔截所有未知的錯誤 (兜底)
    // Debug only: print stack trace
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllUncaughtException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error has occurred. Please contact the administrator." + ex.getMessage());
        problem.setTitle("Internal Server Error");
        return problem;
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // 收集所有欄位的錯誤訊息
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            // 如果同一個欄位有多個錯誤，後面的會覆蓋前面的，或者你可以改成 List
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
