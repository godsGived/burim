package com.burim.product_service.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Long categoryId,
        Long brandId,
        Integer stock,
        BigDecimal rating,
        Integer reviewsCount,
        OffsetDateTime createdAt
) {

}
