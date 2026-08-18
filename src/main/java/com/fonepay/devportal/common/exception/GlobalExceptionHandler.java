package com.fonepay.devportal.common.exception;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fonepay.devportal.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), Collections.singletonList(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), Collections.singletonList(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), Collections.singletonList(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), Collections.singletonList(ex.getMessage()));
    }

    @ExceptionHandler(InvalidOrExpiredTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOrExpiredTokenException(InvalidOrExpiredTokenException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), Collections.singletonList(ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyVerifiedException(EmailAlreadyVerifiedException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), Collections.singletonList(ex.getMessage()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse<Void>> handleTooManyRequestsException(TooManyRequestsException ex) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), Collections.singletonList(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred",
                Collections.singletonList(ex.getMessage()));
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus status,
            String message,
            List<String> errors) {

        return ResponseEntity.status(status)
                .body(ApiResponse.<Void>builder()
                        .status(status.value())
                        .success(false)
                        .message(message)
                        .errors(errors)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }
}
