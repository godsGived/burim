package com.burim.cart_service.controller;

import com.burim.cart_service.BaseIntegrationTest;
import com.burim.cart_service.dto.CartItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void shouldAddItemAndGetCart() throws Exception {
        String userId = "user-123";
        CartItemRequest request = new CartItemRequest(1L, 3);

        mockMvc.perform(post("/api/v1/cart/items")
                        .with(jwt().jwt(builder -> builder.subject(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(3));

        mockMvc.perform(get("/api/v1/cart")
                        .with(jwt().jwt(builder -> builder.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3));
    }

    @Test
    void shouldClearCartViaInternalEndpoint() throws Exception {
        String userId = "user-internal";
        redisTemplate.opsForHash().put("cart:" + userId, "1", "2");

        mockMvc.perform(delete("/api/v1/internal/cart/{userId}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/internal/cart/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void shouldRejectZeroOrNegativeQuantity() throws Exception {
        mockMvc.perform(put("/api/v1/cart/items/1")
                        .with(jwt())
                        .param("quantity", "0"))
                .andExpect(status().isBadRequest());
    }
}