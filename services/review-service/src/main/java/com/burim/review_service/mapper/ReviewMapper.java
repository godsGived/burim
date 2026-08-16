package com.burim.review_service.mapper;

import com.burim.review_service.dto.CreateReviewRequest;
import com.burim.review_service.dto.ReviewResponse;
import com.burim.review_service.enitiy.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toReview(Long userId, CreateReviewRequest reviewRequest) {
        return Review.builder()
                .userId(userId)
                .productId(reviewRequest.productId())
                .rating(reviewRequest.rating())
                .title(reviewRequest.title())
                .description(reviewRequest.description())
                .advantages(reviewRequest.advantages())
                .disadvantages(reviewRequest.disadvantages())
                .build();
    }

    public ReviewResponse toResponse(Review review){
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                review.getProductId(),
                review.getRating(),
                review.getTitle(),
                review.getDescription(),
                review.getAdvantages(),
                review.getDisadvantages(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

}
