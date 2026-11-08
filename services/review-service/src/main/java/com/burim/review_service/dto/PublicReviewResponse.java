package com.burim.review_service.dto;

import java.time.OffsetDateTime;

public record PublicReviewResponse(
        Long id,
        Long productId,
        Integer rating,
        String title,
        String description,
        String advantages,
        String disadvantages,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
