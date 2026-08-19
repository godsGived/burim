package com.burim.order_service.dto;

import java.util.List;

public record ErrorResponse<T> (
        String timestamp,
        int status,
        String error,
        String message,
        T details
) {}