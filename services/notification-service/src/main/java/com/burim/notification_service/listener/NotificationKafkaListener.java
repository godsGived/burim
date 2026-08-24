package com.burim.notification_service.listener;

import com.burim.notification_service.client.ProductServiceClient;
import com.burim.notification_service.dto.ProductSummaryResponse;
import com.burim.notification_service.event.OrderCreatedEvent;
import com.burim.notification_service.event.OrderStatusChangedEvent;
import com.burim.notification_service.event.ReviewEvent;
import com.burim.notification_service.event.ReviewEventType;
import com.burim.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaListener {

    private final NotificationService notificationService;
    private final ProductServiceClient productServiceClient;

    @KafkaListener(topics = "product-reviews-topic", groupId = "notification-service-group")
    public void handleReviewEvent(ReviewEvent event) {
        log.info("Received review event type {} for product {}", event.eventType(), event.productId());

        if (event.eventType() == ReviewEventType.REVIEW_CREATED) {
            var productOpt = productServiceClient.getProductById(event.productId());

            String sellerId = productOpt.filter(p -> p.sellerId() != null)
                    .map(ProductSummaryResponse::sellerId)
                    .orElse("seller-default");

            String productTitle = productOpt.filter(p -> p.title() != null)
                    .map(ProductSummaryResponse::title)
                    .orElse("Товар №" + event.productId());

            notificationService.createNotification(
                    sellerId,
                    "NEW_PRODUCT_REVIEW",
                    "Новый отзыв на ваш товар!",
                    String.format("Покупатель оставил отзыв (%d/5) на товар «%s».", event.rating(), productTitle)
            );

            if (event.userId() != null) {
                notificationService.createNotification(
                        event.userId(),
                        "REVIEW_PUBLISHED",
                        "Ваш отзыв опубликован!",
                        String.format("Спасибо за оценку %d/5 товару «%s».", event.rating(), productTitle)
                );
            }
        }
    }

    @KafkaListener(topics = "order-created-topic", groupId = "notification-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order created event for orderrr {}", event);
        notificationService.createNotification(
                event.userId(),
                "ORDER_CREATED",
                "Заказ успешно оформлен!",
                String.format("Заказ №%d на сумму %s руб. принят в обработку.", event.orderId(), event.totalAmount())
        );
    }

    // TODO: Connect with order-service status update flow once PATCH /orders/{id}/status is implemented
    @KafkaListener(topics = "order-status-topic", groupId = "notification-service-group")
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Received status change event for order {}", event.orderId());
        notificationService.createNotification(
                event.userId(),
                "ORDER_STATUS_CHANGED",
                "Статус заказа изменен",
                String.format("Статус заказа №%d изменился: %s -> %s", event.orderId(), event.oldStatus(), event.newStatus())
        );
    }
}