package com.burim.notification_service.dto;

import com.burim.notification_service.entity.Notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String userId,
        String type,
        String title,
        String message,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse fromEntity(Notification entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.isRead(),
                entity.getCreatedAt()
        );
    }
}