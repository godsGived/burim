package com.burim.product_service.controller;

import com.burim.product_service.BaseIntegrationTest;
import com.burim.product_service.dto.ProductRequest;
import com.burim.product_service.dto.ProductResponse;
import com.burim.product_service.entity.Product;
import com.burim.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ProductControllerIT extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }


    @Test
    void createProduct_WhenRequestIsValid_ShouldSaveProductAndReturn201() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest(
                "Mechanical Keyboard",
                "RGB Gaming Keyboard",
                new BigDecimal("129.99"), 1L,
                1L,
                12
        );
        // Act
        var response = mockMvc.perform(post("/api/v1/products")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.price").value(129.99))
                .andReturn();
        // Assert
        String responseJson = response.getResponse().getContentAsString();
        var responseDto = objectMapper.readValue(responseJson, ProductResponse.class);

        var product = productRepository.findById(responseDto.id());

        assertThat(product).isPresent();

        Product savedProduct = product.get();

        assertThat(savedProduct.getName()).isEqualTo("Mechanical Keyboard");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("129.99"));
        assertThat(savedProduct.getStock()).isEqualTo(12);
        assertThat(savedProduct.getCategory().getId()).isEqualTo(1L);
        assertThat(savedProduct.getBrand().getId()).isEqualTo(1L);
    }


    @Test
    void createProduct_WhenBrandIdDoesNotExists_ShouldThrowBrandNotFoundException() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest(
                "Mechanical Keyboard",
                "RGB Gaming Keyboard",
                new BigDecimal("129.99"), 1L,
                9999L,
                12
        );
        // Act
        var response = mockMvc.perform(post("/api/v1/products")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Brand not found with id: 9999"))
                .andExpect(jsonPath("$.id").doesNotExist());

        // Assert
        assertThat(productRepository.count()).isZero();
    }

    @Test
    void createProduct_WhenUnauthenticated_ShouldReturn401() throws Exception {
        ProductRequest request = new ProductRequest(
                "Mechanical Keyboard",
                "RGB Gaming Keyboard",
                new BigDecimal("129.99"), 1L, 1L, 12
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        assertThat(productRepository.count()).isZero();
    }

    @Test
    void updateProduct_WhenAuthenticated_ShouldUpdateProduct() throws Exception {
        ProductRequest createRequest = new ProductRequest(
                "Mechanical Keyboard",
                "RGB Gaming Keyboard",
                new BigDecimal("129.99"), 1L, 1L, 12
        );

        var createResponse = mockMvc.perform(post("/api/v1/products")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long productId = objectMapper.readValue(
                createResponse.getResponse().getContentAsString(),
                ProductResponse.class
        ).id();

        ProductRequest updateRequest = new ProductRequest(
                "Mechanical Keyboard K2",
                "Updated description",
                new BigDecimal("149.99"), 1L, 1L, 8
        );

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard K2"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.stock").value(8));

        var updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Mechanical Keyboard K2");
        assertThat(updated.getStock()).isEqualTo(8);
    }
}
