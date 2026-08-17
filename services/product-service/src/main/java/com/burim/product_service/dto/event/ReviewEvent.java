package com.burim.product_service.dto.event;

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
) {}