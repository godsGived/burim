package com.burim.notification_service.event;

import java.time.Instant;
import java.util.UUID;

public record ReviewEvent(
        UUID eventId,
        ReviewEventType eventType,
        Long reviewId,
        Long productId,
        String userId,
        Integer rating,
        Long version,
        Instant timestamp
){}