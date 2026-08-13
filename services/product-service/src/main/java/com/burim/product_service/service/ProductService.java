package com.burim.product_service.service;

import com.burim.product_service.dto.ProductRequest;
import com.burim.product_service.dto.ProductResponse;
import com.burim.product_service.entity.Product;
import com.burim.product_service.exceptions.ProductNotFoundException;
import com.burim.product_service.mapper.ProductMapper;
import com.burim.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id){
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    public ProductResponse createProduct(ProductRequest request){
        Product savedProduct = productRepository.save(productMapper.toProduct(request));
        return productMapper.toResponse(savedProduct);
    }

    public void deleteProduct(Long id){
        Product product = productRepository.findById(id)
                        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

}
