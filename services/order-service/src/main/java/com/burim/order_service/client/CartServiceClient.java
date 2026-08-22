package com.burim.order_service.client;

import com.burim.order_service.dto.CartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartServiceClient {

    private final RestClient cartRestClient;

    public CartResponse getCart(String userId){
        return cartRestClient.get()
                .uri("/api/v1/internal/cart/{userId}", userId)
                .retrieve()
                .body(CartResponse.class);
    }

    public void clearCart(String userId) {
        try {
            cartRestClient.delete()
                    .uri("/api/v1/internal/cart/{userId}", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.error("Failed to clear cart for userId: {}", userId, ex);
        }
    }

}
