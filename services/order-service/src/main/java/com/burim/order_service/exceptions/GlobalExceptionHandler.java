package com.burim.order_service.exceptions;

import com.burim.order_service.dto.ErrorResponse;
import com.burim.order_service.dto.StockShortageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ErrorResponse<Void>> handleEmptyCart(EmptyCartException ex) {
        log.warn("Checkout failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        "EMPTY_CART",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse<List<StockShortageDto>>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Stock reservation failed: insufficient stock");
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse<>(
                        HttpStatus.CONFLICT.value(),
                        "INSUFFICIENT_STOCK",
                        "Some items are out of stock",
                        ex.getShortages() // или ex.getDetails()
                ));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse<List<Long>>> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("Stock reservation failed: product not found");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse<>(
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_NOT_FOUND",
                        "Requested products not found",
                        ex.getMissingIds() // или ex.getDetails()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_FAILED",
                        "Validation error on input fields",
                        errors
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception in order service: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse<>(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred: " + ex.getMessage()
                ));
    }
}