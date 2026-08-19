package com.burim.order_service.dto;

public record StockShortageDto(
        Long productId,
        String productName,
        Integer requestedQuantity,
        Integer availableQuantity
) {}