package com.burim.review_service.controller;

import com.burim.review_service.dto.CreateReviewRequest;
import com.burim.review_service.dto.ReviewResponse;
import com.burim.review_service.dto.UpdateReviewRequest;
import com.burim.review_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReviewRequest reviewRequest) {
        String userId = jwt.getSubject();
        return reviewService.createReview(userId, reviewRequest);
    }

    @GetMapping("/products/{productId}")
    public List<ReviewResponse> getProductReviews(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
    }

    @GetMapping("/my")
    public List<ReviewResponse> getUserReviews(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        return reviewService.getUserReviews(userId);
    }

    @GetMapping("/{id}")
    public ReviewResponse getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @PutMapping("/{id}")
    public ReviewResponse updateReview(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        String userId = jwt.getSubject();
        return reviewService.updateReview(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        reviewService.deleteReview(id, userId);
    }
}
