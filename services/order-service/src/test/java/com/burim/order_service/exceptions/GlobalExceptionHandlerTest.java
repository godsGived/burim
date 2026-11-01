package com.burim.order_service.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dataIntegrityViolation_ShouldReturn409Conflict() {
        var response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key value violates unique constraint")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().error()).isEqualTo("DATA_INTEGRITY_VIOLATION");
        assertThat(response.getBody().message()).isEqualTo("Request conflicts with existing data");
    }

    @Test
    void orderNotFound_ShouldReturn404() {
        var response = handler.handleOrderNotFound(new OrderNotFoundException(42L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().error()).isEqualTo("ORDER_NOT_FOUND");
    }

    @Test
    void accessDenied_ShouldReturn403() {
        var response = handler.handleAccessDenied(new AccessDeniedException("Not your order"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getBody().error()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void orderStatusConflict_ShouldReturn409() {
        var response = handler.handleOrderStatusConflict(new OrderStatusConflictException("Cannot cancel"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().error()).isEqualTo("ORDER_STATUS_CONFLICT");
    }

    @Test
    void serviceUnavailable_ShouldReturn503() {
        var response = handler.handleServiceUnavailable(new ServiceUnavailableException("Downstream error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(response.getBody().error()).isEqualTo("SERVICE_UNAVAILABLE");
    }
}
