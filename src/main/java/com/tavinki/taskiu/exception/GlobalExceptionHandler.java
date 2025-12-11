package com.tavinki.taskiu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.Objects;

// 1. 加上這個註解，表示這是一個全域處理器
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 2. 定義要攔截什麼 Exception (這裡是自定義的 UserAlreadyExistsException)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserExists(UserAlreadyExistsException ex) {

        // 3. 建立標準錯誤回應 (Spring Boot 3 寫法)
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, // 設定 HTTP 狀態碼 (409)
                ex.getMessage() // 設定錯誤訊息
        );

        // 設定錯誤標題
        problem.setTitle("User Already Exists");
        // 設定錯誤類型 ID (給前端判斷用)
        problem.setType(Objects.requireNonNull(URI.create("urn:taskiu:errors:user-exists")));

        // 你甚至可以加自定義欄位
        problem.setProperty("timestamp", System.currentTimeMillis());

        return problem;
    }

    // 4. 攔截所有未知的錯誤 (兜底)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllUncaughtException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error has occurred. Please contact the administrator.");
        problem.setTitle("Internal Server Error");
        return problem;
    }
}
