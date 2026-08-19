package com.burim.product_service.dto;

public record StockShortage(
        Long productId,
        String productName,
        Integer requestedQuantity,
        Integer availableQuantity
) {
}
