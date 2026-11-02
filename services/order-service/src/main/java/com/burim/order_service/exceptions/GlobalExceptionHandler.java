package com.burim.order_service.exceptions;

import com.burim.order_service.dto.ErrorResponse;
import com.burim.order_service.dto.StockShortageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
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

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<ErrorResponse<Void>> handleReservationConflict(ReservationConflictException ex) {
        log.warn("Stock reservation conflict: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse<>(
                        HttpStatus.CONFLICT.value(),
                        "RESERVATION_CONFLICT",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Order data integrity violation: {}", ex.getMostSpecificCause().getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse<>(
                        HttpStatus.CONFLICT.value(),
                        "DATA_INTEGRITY_VIOLATION",
                        "Request conflicts with existing data"
                ));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse<Void>> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse<>(
                        HttpStatus.NOT_FOUND.value(),
                        "ORDER_NOT_FOUND",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse<>(
                        HttpStatus.FORBIDDEN.value(),
                        "ACCESS_DENIED",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(OrderStatusConflictException.class)
    public ResponseEntity<ErrorResponse<Void>> handleOrderStatusConflict(OrderStatusConflictException ex) {
        log.warn("Order status conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse<>(
                        HttpStatus.CONFLICT.value(),
                        "ORDER_STATUS_CONFLICT",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse<Void>> handleServiceUnavailable(ServiceUnavailableException ex) {
        log.warn("Service unavailable: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse<>(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "SERVICE_UNAVAILABLE",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        "MISSING_HEADER",
                        "Required header missing: " + ex.getHeaderName()
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