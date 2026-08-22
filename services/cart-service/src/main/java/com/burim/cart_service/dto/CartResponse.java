package com.burim.cart_service.dto;

import java.util.List;

public record CartResponse(
        String userId,
        List<CartItemResponse> items
) {}