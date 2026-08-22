package com.burim.order_service;

import com.burim.order_service.entity.OrderStatus;
import com.burim.order_service.repository.OrderRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderCheckoutWireMockTest extends BaseIntegrationTest {

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

        wiremock.stubFor(WireMock.post(urlMatching("/api/v1/internal/products/reservations/.*/release"))
                .willReturn(noContent()));

        doThrow(new RuntimeException("Database disk failure")).when(orderRepository).save(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders/checkout")
                        .with(jwt().jwt(b -> b.subject(userId))))
                .andExpect(status().isInternalServerError());

        wiremock.verify(1, postRequestedFor(urlMatching("/api/v1/internal/products/reservations/.*/release")));
        wiremock.verify(0, deleteRequestedFor(urlEqualTo("/api/v1/internal/cart/" + userId)));
        assertThat(orderRepository.findAll()).isEmpty();
    }
}