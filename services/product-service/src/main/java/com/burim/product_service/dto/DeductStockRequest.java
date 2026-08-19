package com.burim.product_service.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeductStockRequest(
        Long productId,
        @NotNull
        @Positive
        Integer quantity
) {
}
