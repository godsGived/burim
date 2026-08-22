package com.burim.order_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse<T>(
        String timestamp,
        int status,
        String error,
        String message,
        T details
) {
    public ErrorResponse(int status, String error, String message, T details) {
        this(Instant.now().toString(), status, error, message, details);
    }

    public ErrorResponse(int status, String error, String message) {
        this(Instant.now().toString(), status, error, message, null);
    }
}