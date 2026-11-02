package com.burim.order_service.service;

import com.burim.order_service.client.CartServiceClient;
import com.burim.order_service.client.ProductServiceClient;
import com.burim.order_service.dto.*;
import com.burim.order_service.entity.Order;
import com.burim.order_service.entity.OrderStatus;
import com.burim.order_service.exceptions.AccessDeniedException;
import com.burim.order_service.exceptions.EmptyCartException;
import com.burim.order_service.exceptions.OrderNotFoundException;
import com.burim.order_service.exceptions.OrderStatusConflictException;
import com.burim.order_service.exceptions.ServiceUnavailableException;
import com.burim.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final CartServiceClient cartServiceClient;
    private final OrderWriter orderWriter;

    public OrderResponse createOrder(String userId, String idempotencyKey, CreateOrderRequest request) {
        if (idempotencyKey != null) {
            var existing = orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent order request: order {} already exists for key {}", existing.get().getId(), idempotencyKey);
                return mapToResponse(existing.get());
            }
        }

        UUID operationId = UUID.randomUUID();

        List<DeductStockRequest> deductRequests = request.items().stream()
                .map(r -> new DeductStockRequest(r.productId(), r.quantity()))
                .toList();

        List<ProductStockSnapshot> stockResponse = productServiceClient.reserveStock(operationId, deductRequests);

        Order savedOrder;
        try {
            savedOrder = orderWriter.create(userId, idempotencyKey, operationId, stockResponse);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Race condition on idempotency key: {}", idempotencyKey);
            compensateReservation(operationId);
            if (idempotencyKey != null) {
                return orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                        .map(this::mapToResponse)
                        .orElseThrow(() -> ex);
            }
            throw ex;
        } catch (Exception ex) {
            compensateReservation(operationId);
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

    public OrderResponse checkout(String userId, String idempotencyKey) {
        if (idempotencyKey != null) {
            var existing = orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent checkout: returning existing order {}", existing.get().getId());
                return mapToResponse(existing.get());
            }
        }

        CartResponse cart = cartServiceClient.getCart(userId);

        if (cart.items() == null || cart.items().isEmpty()) {
            throw new EmptyCartException("Cannot checkout an empty cart");
        }

        List<OrderItemRequest> items = cart.items().stream()
                .map(i -> new OrderItemRequest(i.productId(), i.quantity()))
                .toList();

        OrderResponse order = createOrder(userId, idempotencyKey, new CreateOrderRequest(items));

        try {
            cartServiceClient.clearCart(userId);
        } catch (Exception ex) {
            log.warn("Failed to clear cart for user {} after order creation", userId, ex);
        }

        return order;
    }

    public OrderResponse cancelOrder(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to cancel this order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.info("Order {} is already cancelled, returning idempotently", orderId);
            return mapToResponse(order);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderStatusConflictException("Cannot cancel order with status: " + order.getStatus());
        }

        if (order.getOperationId() != null) {
            try {
                productServiceClient.releaseReservation(order.getOperationId());
            } catch (Exception ex) {
                log.error("Failed to release reservation for order {} (operationId: {})", orderId, order.getOperationId(), ex);
                throw new ServiceUnavailableException("Failed to release reservation. Please retry.");
            }
        }

        Order cancelledOrder = orderWriter.cancel(order);
        log.info("Order {} cancelled successfully", orderId);
        return mapToResponse(cancelledOrder);
    }

    private void compensateReservation(UUID operationId) {
        try {
            productServiceClient.releaseReservation(operationId);
        } catch (Exception e) {
            log.error("Failed to release stock reservation during checkout compensation for operationId: {}", operationId, e);
        }
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
