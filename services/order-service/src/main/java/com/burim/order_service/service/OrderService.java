package com.burim.order_service.service;

import com.burim.order_service.client.CartServiceClient;
import com.burim.order_service.client.ProductServiceClient;
import com.burim.order_service.dto.*;
import com.burim.order_service.entity.Order;
import com.burim.order_service.entity.OrderItem;
import com.burim.order_service.entity.OrderStatus;
import com.burim.order_service.event.OrderCreatedEvent;
import com.burim.order_service.exceptions.EmptyCartException;
import com.burim.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final CartServiceClient cartServiceClient;
    private final OrderEventProducer orderEventProducer;

    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        UUID operationId = UUID.randomUUID();

        List<DeductStockRequest> deductRequests = request.items().stream()
                .map(r -> new DeductStockRequest(r.productId(), r.quantity()))
                .toList();

        List<ProductStockSnapshot> stockResponse = productServiceClient.reserveStock(operationId, deductRequests);

        try {
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

            OrderCreatedEvent event = new OrderCreatedEvent(
                    savedOrder.getId(),
                    savedOrder.getUserId(),
                    savedOrder.getTotalAmount(),
                    savedOrder.getItems().stream()
                            .map(item -> new OrderCreatedEvent.OrderItemEvent(
                                    item.getProductId(),
                                    item.getQuantity(),
                                    item.getUnitPrice()))
                            .toList(),
                    savedOrder.getCreatedAt()
            );

            orderEventProducer.sendOrderCreatedEvent(event);

            return mapToResponse(savedOrder);

        } catch (Exception ex) {
            productServiceClient.releaseReservation(operationId);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String userId) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse checkout(String userId) {
        CartResponse cart = cartServiceClient.getCart(userId);

        if (cart.items() == null || cart.items().isEmpty()) {
            throw new EmptyCartException("Cannot checkout an empty cart");
        }

        List<OrderItemRequest> items = cart.items().stream()
                .map(i -> new OrderItemRequest(i.productId(), i.quantity()))
                .toList();

        OrderResponse order = createOrder(userId, new CreateOrderRequest(items));

        cartServiceClient.clearCart(userId);

        return order;
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}