package com.burim.order_service.event;

import com.burim.order_service.service.OrderEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCreatedEventListenerTest {

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderCreatedEventListener listener;

    @Test
    void handle_ShouldDelegateToKafkaProducer() {
        OrderCreatedEvent event = orderCreatedEvent();

        listener.handle(event);

        verify(orderEventProducer).sendOrderCreatedEvent(event);
    }

    @Test
    void handle_WhenKafkaProducerFails_ShouldNotPropagateException() {
        OrderCreatedEvent event = orderCreatedEvent();
        doThrow(new RuntimeException("Kafka is unavailable"))
                .when(orderEventProducer).sendOrderCreatedEvent(event);

        assertThatCode(() -> listener.handle(event)).doesNotThrowAnyException();
    }

    private OrderCreatedEvent orderCreatedEvent() {
        return new OrderCreatedEvent(
                1L,
                "user-1",
                new BigDecimal("100.00"),
                List.of(new OrderCreatedEvent.OrderItemEvent(1L, 1, new BigDecimal("100.00"))),
                Instant.now()
        );
    }
}
