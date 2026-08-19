package com.burim.product_service.service;

import com.burim.product_service.dto.*;
import com.burim.product_service.entity.Product;
import com.burim.product_service.exceptions.BrandNotFoundException;
import com.burim.product_service.exceptions.CategoryNotFoundException;
import com.burim.product_service.exceptions.InsufficientStockException;
import com.burim.product_service.exceptions.ProductNotFoundException;
import com.burim.product_service.mapper.ProductMapper;
import com.burim.product_service.repository.BrandRepository;
import com.burim.product_service.repository.CategoryRepository;
import com.burim.product_service.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new ProductNotFoundException(id));
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
                        .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.delete(product);
    }

    @Transactional
    public List<ProductStockSnapshot> deductStock(@Valid List<DeductStockRequest> requests) {
        Map<Long, Integer> requestHM = requests.stream()
                .collect(Collectors.toMap(
                        DeductStockRequest::productId,
                        DeductStockRequest::quantity,
                        Integer::sum
                ));

        List<Long> productIds = requestHM.keySet().stream()
                .sorted()
                .toList();

        List<Product> products = productRepository.findAllByIdIn(productIds);

        if (products.size() != productIds.size()) {
            Set<Long> foundIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingIds = new HashSet<>(productIds);
            missingIds.removeAll(foundIds);

            throw new ProductNotFoundException(missingIds);
        }

        List<StockShortage> shortages = new ArrayList<>();

        for (Product product : products) {
            int requested = requestHM.get(product.getId());
            int available = product.getStock();

            if (available < requested) {
                shortages.add(new StockShortage(
                        product.getId(),
                        product.getName(),
                        requested,
                        available
                ));
            }
        }

        if (!shortages.isEmpty()) {
            throw new InsufficientStockException(shortages);
        }

        return products.stream()
                .map(product -> {
                    int requestedQty = requestHM.get(product.getId());
                    product.setStock(product.getStock() - requestedQty);

                    return new ProductStockSnapshot(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            requestedQty
                    );
                })
                .toList();
    }
}
