package com.burim.notification_service.event;

public record OrderStatusChangedEvent(
        Long orderId,
        String userId,
        String oldStatus,
        String newStatus
) {}