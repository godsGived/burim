package com.burim.order_service.dto;

public record DeductStockRequest(
        Long productId,
        Integer quantity
) {}