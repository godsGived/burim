package com.burim.order_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CartClientConfig {

    @Bean
    public RestClient cartRestClient(@Value("${services.cart-service.url}") String cartServiceUrl) {
        return RestClient.builder()
                .baseUrl(cartServiceUrl)
                .build();
    }
}