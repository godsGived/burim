package com.burim.review_service.controller;

import com.burim.review_service.dto.CreateReviewRequest;
import com.burim.review_service.dto.ReviewResponse;
import com.burim.review_service.dto.UpdateReviewRequest;
import com.burim.review_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateReviewRequest reviewRequest) {
        return reviewService.createReview(userId, reviewRequest);
    }

    @GetMapping("/products/{productId}")
    public List<ReviewResponse> getProductReviews(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
    }

    @GetMapping("/my")
    public List<ReviewResponse> getUserReviews(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return reviewService.getUserReviews(userId);
    }

    @GetMapping("/{id}")
    public ReviewResponse getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @PutMapping("/{id}")
    public ReviewResponse updateReview(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        return reviewService.updateReview(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        reviewService.deleteReview(id, userId);
    }
}
