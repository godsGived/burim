package com.burim.order_service.repository;

import com.burim.order_service.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"items"})
    List<Order> findAllByUserIdOrderByCreatedAtDesc(String userId);

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findByUserIdAndIdempotencyKey(String userId, String idempotencyKey);

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findByOperationId(UUID operationId);
}
