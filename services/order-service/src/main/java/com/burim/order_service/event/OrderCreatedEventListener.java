package com.burim.order_service.event;

import com.burim.order_service.service.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private final OrderEventProducer orderEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCreatedEvent event) {
        try {
            orderEventProducer.sendOrderCreatedEvent(event);
        } catch (Exception ex) {
            log.error("Failed to publish order-created event for order {}", event.orderId(), ex);
        }
    }
}
