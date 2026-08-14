package com.burim.product_service.service;

import com.burim.product_service.dto.ProductRequest;
import com.burim.product_service.dto.ProductResponse;
import com.burim.product_service.entity.Product;
import com.burim.product_service.exceptions.BrandNotFoundException;
import com.burim.product_service.exceptions.CategoryNotFoundException;
import com.burim.product_service.exceptions.ProductNotFoundException;
import com.burim.product_service.mapper.ProductMapper;
import com.burim.product_service.repository.BrandRepository;
import com.burim.product_service.repository.CategoryRepository;
import com.burim.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
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

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        var brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + request.brandId()));

        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + request.categoryId()));

        var product = productMapper.toProduct(request);
        product.setBrand(brand);
        product.setCategory(category);

        var savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id){
        Product product = productRepository.findById(id)
                        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

}
