package com.burim.product_service.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        String brand,
        Integer stock,
        OffsetDateTime createdAt
) {
}
