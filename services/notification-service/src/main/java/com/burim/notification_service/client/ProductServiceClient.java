package com.burim.notification_service.client;

import com.burim.notification_service.dto.ProductSummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@Slf4j
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(@Value("${services.product.url:http://localhost:8081}") String productServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .build();
    }

    public Optional<ProductSummaryResponse> getProductById(Long productId) {
        try {
            ProductSummaryResponse response = restClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(ProductSummaryResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.warn("Failed to fetch product #{} from product-service: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }
}