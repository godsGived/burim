package com.burim.review_service.service;

import com.burim.review_service.dto.event.ReviewEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReviewEventProducer {

    private final KafkaTemplate<String, ReviewEvent> kafkaTemplate;
    private final String topicName;

    public ReviewEventProducer(KafkaTemplate<String, ReviewEvent> kafkaTemplate,
                               @Value("${app.kafka.topics.reviews}") String topicName){
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void sendReviewEvent(ReviewEvent reviewEvent){
        kafkaTemplate.send(topicName, String.valueOf(reviewEvent.productId()), reviewEvent);    }
}
