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
public class OrderStatusChangedEventListener {

    private final OrderEventProducer orderEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderStatusChangedEvent event) {
        try {
            orderEventProducer.sendOrderStatusChangedEvent(event);
        } catch (Exception ex) {
            log.error("Failed to publish order status event for order {}", event.orderId(), ex);
        }
    }
}
