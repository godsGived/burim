package com.burim.order_service.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        List<OrderItemEvent> items,
        Instant createdAt
) {
    public record OrderItemEvent(
            Long productId,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}