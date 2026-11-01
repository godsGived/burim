package com.burim.order_service.service;

import com.burim.order_service.dto.ProductStockSnapshot;
import com.burim.order_service.entity.Order;
import com.burim.order_service.entity.OrderItem;
import com.burim.order_service.entity.OrderStatus;
import com.burim.order_service.event.OrderCreatedEvent;
import com.burim.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderWriter {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order create(String userId, List<ProductStockSnapshot> stockResponse) {
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

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getTotalAmount(),
                orderItems.stream()
                        .map(item -> new OrderCreatedEvent.OrderItemEvent(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getUnitPrice()))
                        .toList(),
                savedOrder.getCreatedAt()
        ));

        return savedOrder;
    }
}
