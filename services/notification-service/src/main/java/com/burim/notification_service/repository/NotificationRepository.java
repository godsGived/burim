package com.burim.notification_service.repository;

import com.burim.notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findAllByUserIdAndReadFalseOrderByCreatedAtDesc(String userId);
}