package com.bizkredit.exception;

import com.bizkredit.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// Handles all exceptions globally - no need for try-catch in controllers
// Uses Java 21 pattern matching switch for clean exception handling
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {

        // Java 21 pattern matching switch - matches exception type and extracts variable
        return switch (ex) {

            // 404 - resource not found
            case ResourceNotFoundException e -> {
                log.warn("Not found: {}", e.getMessage());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(e.getMessage()));
            }

            // 400 - business rule violation
            case BadRequestException e -> {
                log.warn("Bad request: {}", e.getMessage());
                yield ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(e.getMessage()));
            }

            // 400 - @Valid annotation failures
            case MethodArgumentNotValidException e -> {
                var errors = e.getBindingResult().getFieldErrors().stream()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .collect(Collectors.joining(", "));
                log.warn("Validation failed: {}", errors);
                yield ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(errors));
            }

            // 401 - not authenticated
            case AuthenticationException e -> {
                log.warn("Authentication failed: {}", e.getMessage());
                yield ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid credentials"));
            }

            // 403 - authenticated but not authorized
            case AccessDeniedException e -> {
                log.warn("Access denied: {}", e.getMessage());
                yield ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied"));
            }

            // 500 - unexpected errors
            default -> {
                log.error("Unexpected error: {}", ex.getMessage(), ex);
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Something went wrong. Please try again."));
            }
        };
    }
}
