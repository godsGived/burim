package com.burim.order_service;

import com.burim.order_service.entity.OrderStatus;
import com.burim.order_service.repository.OrderRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderCheckoutWireMockTest extends BaseIntegrationTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureDownstreamServices(DynamicPropertyRegistry registry) {
        registry.add("services.product-service.url", wiremock::baseUrl);
        registry.add("services.cart-service.url", wiremock::baseUrl);
    }

    @MockitoSpyBean
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        wiremock.resetAll();
    }

    @Test
    void shouldSuccessfullyCheckoutCart() throws Exception {
        String userId = "happy-user";

        wiremock.stubFor(WireMock.get(urlEqualTo("/api/v1/internal/cart/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "userId": "%s",
                                "items": [{ "productId": 1, "quantity": 2 }]
                            }
                        """.formatted(userId))));

        wiremock.stubFor(WireMock.post(urlEqualTo("/api/v1/internal/products/reserve"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            [
                                {
                                    "productId": 1,
                                    "productName": "Клавиатура Keychron",
                                    "price": 100.00,
                                    "quantity": 2
                                }
                            ]
                        """)));

        wiremock.stubFor(WireMock.delete(urlEqualTo("/api/v1/internal/cart/" + userId))
                .willReturn(noContent()));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders/checkout")
                        .with(jwt().jwt(b -> b.subject(userId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()))
                .andExpect(jsonPath("$.totalAmount").value(200.00));

        wiremock.verify(1, deleteRequestedFor(urlEqualTo("/api/v1/internal/cart/" + userId)));
        assertThat(orderRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldReturnConflictWhenStockIsInsufficient() throws Exception {
        String userId = "shortage-user";

        wiremock.stubFor(WireMock.get(urlEqualTo("/api/v1/internal/cart/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "userId": "%s",
                                "items": [{ "productId": 1, "quantity": 10 }]
                            }
                        """.formatted(userId))));

        wiremock.stubFor(WireMock.post(urlEqualTo("/api/v1/internal/products/reserve"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "timestamp": "2026-08-22T12:00:00Z",
                                "status": 409,
                                "error": "INSUFFICIENT_STOCK",
                                "message": "Not enough stock",
                                "details": [
                                    {
                                        "productId": 1,
                                        "productName": "Клавиатура Keychron",
                                        "requestedQuantity": 10,
                                        "availableQuantity": 2
                                    }
                                ]
                            }
                        """)));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders/checkout")
                        .with(jwt().jwt(b -> b.subject(userId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.details[0].productId").value(1))
                .andExpect(jsonPath("$.details[0].productName").value("Клавиатура Keychron"))
                .andExpect(jsonPath("$.details[0].requestedQuantity").value(10))
                .andExpect(jsonPath("$.details[0].availableQuantity").value(2));

        wiremock.verify(0, deleteRequestedFor(urlEqualTo("/api/v1/internal/cart/" + userId)));
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReleaseReservationWhenOrderDatabaseSaveFails() throws Exception {
        String userId = "db-failure-user";

        wiremock.stubFor(WireMock.get(urlEqualTo("/api/v1/internal/cart/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "userId": "%s",
                                "items": [{ "productId": 1, "quantity": 2 }]
                            }
                        """.formatted(userId))));

        wiremock.stubFor(WireMock.post(urlEqualTo("/api/v1/internal/products/reserve"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            [
                                {
                                    "productId": 1,
                                    "productName": "Клавиатура Keychron",
                                    "price": 100.00,
                                    "quantity": 2
                                }
                            ]
                        """)));

        wiremock.stubFor(WireMock.post(urlPathMatching("/api/v1/internal/products/reservations/[a-f0-9\\-]+/release"))
                .willReturn(noContent()));

        doThrow(new RuntimeException("Database disk failure")).when(orderRepository).save(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders/checkout")
                        .with(jwt().jwt(b -> b.subject(userId))))
                .andExpect(status().isInternalServerError());

        wiremock.verify(1, postRequestedFor(urlPathMatching("/api/v1/internal/products/reservations/[a-f0-9\\-]+/release")));
        wiremock.verify(0, deleteRequestedFor(urlEqualTo("/api/v1/internal/cart/" + userId)));
        assertThat(orderRepository.findAll()).isEmpty();
    }
}