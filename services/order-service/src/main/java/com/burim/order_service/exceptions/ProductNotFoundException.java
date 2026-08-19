package com.burim.order_service.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final List<Long> missingIds;

    public ProductNotFoundException(List<Long> missingIds) {
        super("Products not found");
        this.missingIds = missingIds;
    }
}