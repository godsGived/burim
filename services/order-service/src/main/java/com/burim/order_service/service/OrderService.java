package com.burim.order_service.service;

import com.burim.order_service.client.ProductServiceClient;
import com.burim.order_service.dto.*;
import com.burim.order_service.entity.Order;
import com.burim.order_service.entity.OrderItem;
import com.burim.order_service.entity.OrderStatus;
import com.burim.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        List<DeductStockRequest> deductStockRequests = request.items().stream()
                .map(r -> new DeductStockRequest(r.productId(), r.quantity()))
                .toList();

        List<ProductStockSnapshot> stockResponse = productServiceClient.deductStock(deductStockRequests);

        BigDecimal totalAmount = stockResponse.stream()
                .map(s -> s.price().multiply(BigDecimal.valueOf(s.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .userId(userId)
                .totalAmount(totalAmount)
                .build();

        List<OrderItem> orderItems = stockResponse.stream()
                .map(s -> OrderItem.builder()
                        .productId(s.productId())
                        .productName(s.productName())
                        .unitPrice(s.price())
                        .quantity(s.quantity())
                        .build())
                .toList();

        order.addItems(orderItems);

        order = orderRepository.saveAndFlush(order);

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                orderItems.stream()
                        .map(item -> new OrderItemResponse(
                                item.getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity()))
                        .toList()
        );
    }
}