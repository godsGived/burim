package com.burim.order_service.controller;

import com.burim.order_service.dto.CreateOrderRequest;
import com.burim.order_service.dto.OrderResponse;
import com.burim.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping()
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest){
        String userId = "123";
        return orderService.createOrder(userId, createOrderRequest);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@RequestHeader("X-User-Id") String userId) {
        return orderService.checkout(userId);
    }

}
