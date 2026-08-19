package com.burim.order_service.exceptions;

import com.burim.order_service.dto.StockShortageDto;
import lombok.Getter;

import java.util.List;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final List<StockShortageDto> shortages;

    public InsufficientStockException(List<StockShortageDto> shortages) {
        super("Not enough stock for requested items");
        this.shortages = shortages;
    }
}