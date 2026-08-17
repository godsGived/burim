package com.burim.review_service.dto.event;

import java.time.Instant;
import java.util.UUID;

public record ReviewEvent(
        UUID eventId,
        ReviewEventType eventType,
        Long reviewId,
        Long productId,
        Integer rating,
        Long version,
        Instant timestamp
) {
    public static ReviewEvent created(Long reviewId, Long productId, Integer rating, Long version) {
        return new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_CREATED, reviewId, productId, rating, version, Instant.now());
    }

    public static ReviewEvent updated(Long reviewId, Long productId, Integer rating, Long version) {
        return new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_UPDATED, reviewId, productId, rating, version, Instant.now());
    }

    public static ReviewEvent deleted(Long reviewId, Long productId, Long version) {
        return new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_DELETED, reviewId, productId, null, version, Instant.now());
    }
}