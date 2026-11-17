package com.burim.mcp_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    private SimpleClientHttpRequestFactory requestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return factory;
    }

    @Bean
    public RestClient productRestClient(
            @Value("${services.product.url:http://localhost:8081}") String productUrl) {
        return RestClient.builder()
                .baseUrl(productUrl)
                .requestFactory(requestFactory())
                .build();
    }

    @Bean
    public RestClient reviewRestClient(
            @Value("${services.review.url:http://localhost:8084}") String reviewUrl) {
        return RestClient.builder()
                .baseUrl(reviewUrl)
                .requestFactory(requestFactory())
                .build();
    }
}
