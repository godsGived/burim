package com.burim.review_service.service;

import com.burim.review_service.dto.event.ReviewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventAfterCommitListener {

    private final ReviewEventProducer reviewEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReviewEvent event) {
        try {
            reviewEventProducer.sendReviewEvent(event);
        } catch (Exception ex) {
            log.error("Failed to publish review event for review {}", event.reviewId(), ex);
        }
    }
}
