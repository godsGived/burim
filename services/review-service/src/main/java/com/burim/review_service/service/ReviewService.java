package com.burim.review_service.service;

import com.burim.review_service.dto.CreateReviewRequest;
import com.burim.review_service.dto.ReviewResponse;
import com.burim.review_service.dto.UpdateReviewRequest;
import com.burim.review_service.exceptions.AccessDeniedException;
import com.burim.review_service.exceptions.ReviewAlreadyExistsException;
import com.burim.review_service.exceptions.ReviewNotFoundException;
import com.burim.review_service.mapper.ReviewMapper;
import com.burim.review_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;


    @Transactional
    public ReviewResponse createReview(Long userId, CreateReviewRequest reviewRequest) {
        if (reviewRepository.existsByUserIdAndProductId(userId, reviewRequest.productId())) {
            throw new ReviewAlreadyExistsException("Review already exists for product: " + reviewRequest.productId());
        }
        var review = reviewMapper.toReview(userId, reviewRequest);
        var savedReview = reviewRepository.saveAndFlush(review);
        return reviewMapper.toResponse(savedReview);
    }

    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewRepository.findAllByProductId(productId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    public List<ReviewResponse> getUserReviews(Long userId) {
        return reviewRepository.findAllByUserId(userId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    public ReviewResponse getReviewById(Long id) {
        var review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));
        return reviewMapper.toResponse(review);
    }


    @Transactional
    public ReviewResponse updateReview(Long id, Long userId, UpdateReviewRequest request) {
        var review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));

        if (!review.isOwnedBy(userId)) {
            throw new AccessDeniedException("You are not allowed to update this review");
        }

        review.updateContent(
                request.rating(),
                request.title(),
                request.description(),
                request.advantages(),
                request.disadvantages()
        );

        var updatedReview = reviewRepository.saveAndFlush(review);
        return reviewMapper.toResponse(updatedReview);
    }

    @Transactional
    public void deleteReview(Long id, Long userId) {
        var review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));
        if (!review.isOwnedBy(userId)) {
            throw new AccessDeniedException("You are not allowed to delete this review");
        }
        reviewRepository.delete(review);
    }
}
