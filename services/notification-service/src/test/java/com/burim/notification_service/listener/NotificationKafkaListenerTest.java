package com.burim.notification_service.listener;

import com.burim.notification_service.client.ProductServiceClient;
import com.burim.notification_service.dto.ProductSummaryResponse;
import com.burim.notification_service.event.ReviewEvent;
import com.burim.notification_service.event.ReviewEventType;
import com.burim.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private NotificationKafkaListener listener;

    @Test
    void reviewCreated_ShouldUseProductNameAndNotifyOnlyReviewAuthor() {
        ReviewEvent event = reviewCreated(1L, "buyer-1", 5);
        when(productServiceClient.getProductById(1L))
                .thenReturn(Optional.of(new ProductSummaryResponse(1L, "Wireless Mouse")));

        listener.handleReviewEvent(event);

        verify(notificationService).createNotification(
                eq("buyer-1"),
                eq("REVIEW_PUBLISHED"),
                eq("Ваш отзыв опубликован!"),
                contains("Wireless Mouse")
        );
        verify(notificationService, never()).createNotification(
                eq("seller-default"),
                eq("NEW_PRODUCT_REVIEW"),
                eq("Новый отзыв на ваш товар!"),
                contains("Wireless Mouse")
        );
    }

    @Test
    void reviewCreated_WhenProductIsUnavailable_ShouldFallBackToProductId() {
        ReviewEvent event = reviewCreated(42L, "buyer-1", 4);
        when(productServiceClient.getProductById(42L)).thenReturn(Optional.empty());

        listener.handleReviewEvent(event);

        verify(notificationService).createNotification(
                eq("buyer-1"),
                eq("REVIEW_PUBLISHED"),
                eq("Ваш отзыв опубликован!"),
                contains("Товар №42")
        );
    }

    private ReviewEvent reviewCreated(Long productId, String userId, Integer rating) {
        return new ReviewEvent(
                UUID.randomUUID(),
                ReviewEventType.REVIEW_CREATED,
                10L,
                productId,
                userId,
                rating,
                0L,
                Instant.now()
        );
    }
}
