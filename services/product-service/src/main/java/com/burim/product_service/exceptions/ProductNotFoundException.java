package com.burim.product_service.exceptions;

import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
public class ProductNotFoundException extends RuntimeException {

    private final List<Long> missingIds;

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
        this.missingIds = List.of(id);
    }

    public ProductNotFoundException(Collection<Long> missingIds) {
        super("Products not found with ids: " + missingIds) ;
        this.missingIds = List.copyOf(missingIds);
    }
}
