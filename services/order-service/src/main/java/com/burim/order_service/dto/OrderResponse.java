package com.burim.order_service.dto;

import com.burim.order_service.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String userId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        List<OrderItemResponse> items
) {}
