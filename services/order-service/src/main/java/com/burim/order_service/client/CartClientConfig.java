package com.burim.order_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class CartClientConfig {

    @Bean
    public RestClient cartRestClient(
            @Value("${services.cart-service.url}") String cartServiceUrl,
            @Value("${services.cart-service.connect-timeout:2s}") Duration connectTimeout,
            @Value("${services.cart-service.read-timeout:5s}") Duration readTimeout) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(cartServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
