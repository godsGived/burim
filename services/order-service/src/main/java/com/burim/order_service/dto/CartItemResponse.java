package com.burim.order_service.dto;

public record CartItemResponse(
        Long productId,
        Integer quantity
) {}