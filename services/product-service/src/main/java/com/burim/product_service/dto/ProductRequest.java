package com.burim.product_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(
        @NotNull(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price should be greater or equal to 0")
        BigDecimal price,

        @NotNull(message = "Category is required")
        Long categoryId,

        Long brandId,

        @NotNull(message = "Stock is required")
        @PositiveOrZero(message = "Stock should be greater or equal to 0")
        Integer stock
) {
}
