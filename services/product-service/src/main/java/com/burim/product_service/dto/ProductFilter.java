package com.burim.product_service.dto;

import java.math.BigDecimal;

public record ProductFilter(
        String query,
        Long categoryId,
        Long brandId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal minRating,
        Boolean inStock
) {}
