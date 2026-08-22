package com.burim.cart_service.dto;

public record CartItemResponse(
        Long productId,
        Integer quantity
) {}