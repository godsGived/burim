package com.burim.product_service.dto;

import java.math.BigDecimal;

public record ProductStockSnapshot(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity
) {
}
