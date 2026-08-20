package com.burim.product_service.controller;

import com.burim.product_service.dto.ProductStockSnapshot;
import com.burim.product_service.dto.ReserveStockRequest;
import com.burim.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @PostMapping("/reserve")
    public List<ProductStockSnapshot> reserve(@RequestBody ReserveStockRequest request) {
        return productService.reserveStock(request);
    }

    @PostMapping("/reservations/{operationId}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable UUID operationId) {
        productService.releaseReservation(operationId);
    }

}
