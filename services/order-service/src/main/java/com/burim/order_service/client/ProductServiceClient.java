package com.burim.order_service.client;

import com.burim.order_service.dto.*;
import com.burim.order_service.exceptions.InsufficientStockException;
import com.burim.order_service.exceptions.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestClient productRestClient;
    private final ObjectMapper objectMapper;

    public List<ProductStockSnapshot> reserveStock(UUID operationId, List<DeductStockRequest> requests) {
        return productRestClient.post()
                .uri("/api/v1/internal/products/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReserveStockRequest(operationId, requests))
                .retrieve()
                .onStatus(status -> status.value() == 409, (request, response) -> {
                    ErrorResponse<List<StockShortageDto>> error = objectMapper.readValue(
                            response.getBody(),
                            new TypeReference<ErrorResponse<List<StockShortageDto>>>() {}
                    );
                    throw new InsufficientStockException(error.details());
                })
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    ErrorResponse<List<Long>> error = objectMapper.readValue(
                            response.getBody(),
                            new TypeReference<ErrorResponse<List<Long>>>() {}
                    );
                    throw new ProductNotFoundException(error.details());
                })
                .body(new ParameterizedTypeReference<List<ProductStockSnapshot>>() {});
    }

    public void releaseReservation(UUID operationId) {
        try {
            productRestClient.post()
                    .uri("/api/v1/internal/products/reservations/{operationId}/release", operationId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to release stock reservation for operationId: {}", operationId, e);
        }
    }
}