package com.burim.ai_service.exceptions;

import com.burim.ai_service.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_FAILED",
                        "Validation error on input fields",
                        errors
                ));
    }

    @ExceptionHandler({
            TimeoutException.class,
            HttpTimeoutException.class
    })
    public ResponseEntity<ErrorResponse> handleTimeout(Exception ex) {
        log.warn("AI timeout occurred: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse(
                        HttpStatus.GATEWAY_TIMEOUT.value(),
                        "AI_TIMEOUT",
                        "AI request timed out"
                ));
    }

    @ExceptionHandler({
            org.springframework.web.client.ResourceAccessException.class,
            ConnectException.class
    })
    public ResponseEntity<ErrorResponse> handleUnavailable(Exception ex) {
        log.warn("AI dependency unavailable: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "AI_DEPENDENCY_UNAVAILABLE",
                        "MCP server or dependent services are unavailable"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Разворачиваем возможные обёрнутые причины (nested causes)
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof HttpTimeoutException || cause instanceof TimeoutException) {
                return handleTimeout((Exception) cause);
            }
            if (cause instanceof ConnectException || cause instanceof org.springframework.web.client.ResourceAccessException) {
                return handleUnavailable((Exception) cause);
            }
            cause = cause.getCause();
        }

        log.error("Unhandled error in AI service: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred"
                ));
    }
}
