package com.burim.order_service.client;

import com.burim.order_service.dto.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        cartRestClient.delete()
                .uri("/api/v1/internal/cart/{userId}", userId)
                .retrieve()
                .toBodilessEntity();
    }

}
