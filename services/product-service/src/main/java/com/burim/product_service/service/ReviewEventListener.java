package com.burim.product_service.service;

import com.burim.product_service.dto.event.ReviewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewEventListener {

    private final ProductRatingService productRatingService;

    @KafkaListener(
            topics = "${app.kafka.topics.reviews}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(ReviewEvent reviewEvent){
        log.info("Received event: {}", reviewEvent);
        productRatingService.processReviewEvent(reviewEvent);
    }
}
