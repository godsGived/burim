package com.burim.mcp_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Long categoryId,
        Long brandId,
        Integer stock,
        BigDecimal rating,
        Integer reviewsCount
) {
    public ProductDto {
        if (description != null && description.length() > 500) {
            description = description.substring(0, 500) + "...";
        }
    }
}
