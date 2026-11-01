package com.burim.product_service.exceptions;

public class ReservationConflictException extends RuntimeException {

    public ReservationConflictException(String message) {
        super(message);
    }
}
