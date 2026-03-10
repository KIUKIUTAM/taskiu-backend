package com.tavinki.taskiu.common.exceptions;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tavinki.taskiu.common.exceptions.minio.MinioDeleteException;
import com.tavinki.taskiu.common.exceptions.minio.MinioDownloadException;
import com.tavinki.taskiu.common.exceptions.minio.MinioException;
import com.tavinki.taskiu.common.exceptions.minio.MinioFileNotFoundException;
import com.tavinki.taskiu.common.exceptions.minio.MinioUploadException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // -------------------------------------------------------------------------
    // User / Auth Exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserExists(UserAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage());
        problem.setTitle("User Already Exists");
        problem.setType(URI.create("urn:taskiu:errors:user-exists"));
        problem.setProperty("timestamp", System.currentTimeMillis());
        return problem;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Object> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        ProblemDetail errorBody = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
        ResponseCookie clearCookie = clearRefreshTokenCookie();
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
        problem.setType(URI.create("urn:taskiu:errors:email-exists"));
        problem.setProperty("timestamp", System.currentTimeMillis());
        return problem;
    }

    // -------------------------------------------------------------------------
    // MinIO Exceptions
    // 順序很重要：子類別必須排在父類別 MinioException 之前
    // -------------------------------------------------------------------------

    @ExceptionHandler(MinioFileNotFoundException.class)
    public ProblemDetail handleMinioFileNotFound(MinioFileNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
        problem.setTitle("File Not Found");
        problem.setType(URI.create("urn:taskiu:errors:minio-file-not-found"));
        problem.setProperty("timestamp", System.currentTimeMillis());
        return problem;
    }

    @ExceptionHandler(MinioUploadException.class)
    public ProblemDetail handleMinioUpload(MinioUploadException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
        problem.setTitle("File Upload Failed");
        problem.setType(URI.create("urn:taskiu:errors:minio-upload-failed"));
        problem.setProperty("timestamp", System.currentTimeMillis());
        return problem;
    }

    @ExceptionHandler(MinioDownloadException.class)
    public ProblemDetail handleMinioDownload(MinioDownloadException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
        problem.setTitle("File Download Failed");
        problem.setType(URI.create("urn:taskiu:errors:minio-download-failed"));
        problem.setProperty("timestamp", System.currentTimeMillis());
        return problem;
    }

    @ExceptionHandler(MinioDeleteException.class)
    public ProblemDetail handleMinioDelete(MinioDeleteException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
        problem.setTitle("File Delete Failed");
        problem.setType(URI.create("urn:taskiu:errors:minio-delete-failed"));
        problem.setProperty("timestamp", System.currentTimeMillis());
        return problem;
    }

    @ExceptionHandler(MinioException.class)
    public ProblemDetail handleMinio(MinioException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
        problem.setTitle("MinIO Error");
        problem.setType(URI.create("urn:taskiu:errors:minio-error"));
        problem.setProperty("timestamp", System.currentTimeMillis());
        return problem;
    }

    // -------------------------------------------------------------------------
    // Validation & Fallback
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllUncaughtException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error has occurred. Please contact the administrator. " + ex.getMessage());
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
}
