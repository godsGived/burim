package com.burim.order_service.controller;

import com.burim.order_service.dto.CreateOrderRequest;
import com.burim.order_service.dto.OrderResponse;
import com.burim.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
