package com.burim.review_service.repository;

import com.burim.review_service.enitiy.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByProductId(Long productId);
    List<Review> findAllByUserId(String userId);
    boolean existsByUserIdAndProductId(String userId, Long productId);
}
