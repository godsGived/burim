package com.burim.order_service.dto;

import java.math.BigDecimal;

public record ProductStockSnapshot(
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity
) {}