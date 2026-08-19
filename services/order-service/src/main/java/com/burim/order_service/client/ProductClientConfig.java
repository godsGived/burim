package com.burim.order_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ProductClientConfig {

    @Bean
    public RestClient productRestClient(
            @Value("${services.product.url:http://localhost:8081}") String productUrl) {
        return RestClient.builder()
                .baseUrl(productUrl)
                .build();
    }
}