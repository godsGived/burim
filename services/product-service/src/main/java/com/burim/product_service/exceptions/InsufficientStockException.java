package com.burim.product_service.exceptions;

import com.burim.product_service.dto.StockShortage;
import lombok.Getter;

import java.util.List;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final List<StockShortage> shortages;

    public InsufficientStockException(List<StockShortage> shortages) {
        super("Insufficient stock for requested products");
        this.shortages = shortages;
    }
}
