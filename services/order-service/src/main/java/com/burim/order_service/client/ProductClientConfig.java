package com.burim.order_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class ProductClientConfig {

    @Bean
    public RestClient productRestClient(
            @Value("${services.product-service.url:http://localhost:8081}") String productUrl,
            @Value("${services.product-service.connect-timeout:2s}") Duration connectTimeout,
            @Value("${services.product-service.read-timeout:5s}") Duration readTimeout) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(productUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
