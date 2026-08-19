package com.burim.product_service.controller;

import com.burim.product_service.dto.DeductStockRequest;
import com.burim.product_service.dto.ProductStockSnapshot;
import com.burim.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @PostMapping("/deduct-stock")
    public List<ProductStockSnapshot> deductStock(@Valid @RequestBody List<DeductStockRequest> request) {
        return productService.deductStock(request);
    }
}
