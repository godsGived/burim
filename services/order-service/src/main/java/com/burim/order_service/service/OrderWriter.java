package com.burim.order_service.service;

import com.burim.order_service.dto.ProductStockSnapshot;
import com.burim.order_service.entity.Order;
import com.burim.order_service.entity.OrderItem;
import com.burim.order_service.entity.OrderStatus;
import com.burim.order_service.event.OrderCreatedEvent;
import com.burim.order_service.event.OrderStatusChangedEvent;
import com.burim.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderWriter {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order create(String userId, String idempotencyKey, UUID operationId, List<ProductStockSnapshot> stockResponse) {
        BigDecimal totalAmount = stockResponse.stream()
                .map(s -> s.price().multiply(BigDecimal.valueOf(s.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .operationId(operationId)
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

        Order savedOrder = orderRepository.saveAndFlush(order);

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

    @Transactional
    public Order cancel(Order order) {
        String oldStatus = order.getStatus().name();
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.saveAndFlush(order);

        eventPublisher.publishEvent(new OrderStatusChangedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                oldStatus,
                OrderStatus.CANCELLED.name()
        ));

        return savedOrder;
    }
}
