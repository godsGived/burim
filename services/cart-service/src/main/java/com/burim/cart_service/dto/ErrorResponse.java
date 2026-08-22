package com.burim.cart_service.dto;

import java.time.Instant;

public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message
) {
    public ErrorResponse(int status, String error, String message) {
        this(Instant.now().toString(), status, error, message);
    }
}