package com.burim.order_service.service;

import com.burim.order_service.event.OrderCreatedEvent;
import com.burim.order_service.event.OrderStatusChangedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private static final String ORDER_CREATED_TOPIC = "order-created-topic";
    private static final String ORDER_STATUS_TOPIC = "order-status-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send(ORDER_CREATED_TOPIC, String.valueOf(event.orderId()), event);
    }

    public void sendOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        kafkaTemplate.send(ORDER_STATUS_TOPIC, String.valueOf(event.orderId()), event);
    }
}