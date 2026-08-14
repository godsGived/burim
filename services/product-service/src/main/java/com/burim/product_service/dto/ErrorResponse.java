package com.burim.product_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
    public ErrorResponse(int status, String error, String message){
        this(LocalDateTime.now(), status, error, message);
    }
}
