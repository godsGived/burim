package com.burim.order_service.service;

import com.burim.order_service.client.CartServiceClient;
import com.burim.order_service.client.ProductServiceClient;
import com.burim.order_service.dto.*;
import com.burim.order_service.entity.Order;
import com.burim.order_service.exceptions.EmptyCartException;
import com.burim.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final CartServiceClient cartServiceClient;
    private final OrderWriter orderWriter;

    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        UUID operationId = UUID.randomUUID();

        List<DeductStockRequest> deductRequests = request.items().stream()
                .map(r -> new DeductStockRequest(r.productId(), r.quantity()))
                .toList();

        List<ProductStockSnapshot> stockResponse = productServiceClient.reserveStock(operationId, deductRequests);

        Order savedOrder;
        try {
            savedOrder = orderWriter.create(userId, stockResponse);
        } catch (Exception ex) {
            productServiceClient.releaseReservation(operationId);
            throw ex;
        }

        return mapToResponse(savedOrder);
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