package com.burim.product_service.repository;

import com.burim.product_service.entity.ProductReviewState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewStateRepository extends JpaRepository<ProductReviewState, Long> {
}