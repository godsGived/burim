package com.burim.product_service.service;

import com.burim.product_service.dto.ProductResponse;
import com.burim.product_service.entity.Product;
import com.burim.product_service.exceptions.ProductNotFoundException;
import com.burim.product_service.mapper.ProductMapper;
import com.burim.product_service.repository.BrandRepository;
import com.burim.product_service.repository.CategoryRepository;
import com.burim.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductById_WhenProductDoesntExists_ShouldThrowProductNotFoundException(){
        Long id = 999L;

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(id));

    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProductResponse(){
        Long id = 1L;
        var product = Product.builder().id(id).name("MacBook").build();
        var expectedResponse = new ProductResponse(id, "MacBook", null, null, null, null, 0, null, null, null);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(expectedResponse);

        var response = productService.getProductById(id);

        assertEquals(expectedResponse, response);
    }

    @Test
    void getProducts_ShouldReturnPagedResponse() {
        var product = Product.builder().id(1L).name("MacBook").build();
        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var pagedProducts = new org.springframework.data.domain.PageImpl<>(java.util.List.of(product), pageable, 1);
        var expectedResponse = new ProductResponse(1L, "MacBook", null, null, null, null, 0, null, null, null);

        when(productRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Product>>any(), org.mockito.ArgumentMatchers.eq(pageable)))
                .thenReturn(pagedProducts);
        when(productMapper.toResponse(product)).thenReturn(expectedResponse);

        var filter = new com.burim.product_service.dto.ProductFilter("MacBook", null, null, null, null, null, null);
        var result = productService.getProducts(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResponse, result.getContent().get(0));
    }

}