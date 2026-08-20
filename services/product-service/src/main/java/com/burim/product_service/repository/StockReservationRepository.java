package com.burim.product_service.repository;

import com.burim.product_service.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    List<StockReservation> findAllByOperationId(UUID operationId);
}