package com.burim.order_service.client;

import com.burim.order_service.dto.DeductStockRequest;
import com.burim.order_service.dto.ErrorResponse;
import com.burim.order_service.dto.ProductStockSnapshot;
import com.burim.order_service.dto.StockShortageDto;
import com.burim.order_service.exceptions.InsufficientStockException;
import com.burim.order_service.exceptions.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestClient productRestClient;
    private final ObjectMapper objectMapper;

    public List<ProductStockSnapshot> deductStock(List<DeductStockRequest> requests) {
        return productRestClient.post()
                .uri("/api/v1/internal/products/deduct-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requests)
                .retrieve()
                .onStatus(status -> status.value() == 409, ((request, response) -> {
                    ErrorResponse<List<StockShortageDto>> error = objectMapper.readValue(
                            response.getBody(),
                            new TypeReference<ErrorResponse<List<StockShortageDto>>>() {}
                    );
                    throw new InsufficientStockException(error.details());
                }))
                .onStatus(
                        status -> status.value() == 404,
                        (request, response) -> {
                            ErrorResponse<List<Long>> error = objectMapper.readValue(
                                    response.getBody(),
                                    new TypeReference<ErrorResponse<List<Long>>>() {}
                            );
                            throw new ProductNotFoundException(error.details());
                        }
                )
                .body(new ParameterizedTypeReference<>() {});
    }
}