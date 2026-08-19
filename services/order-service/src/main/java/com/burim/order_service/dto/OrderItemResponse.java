package com.burim.order_service.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity
) {}