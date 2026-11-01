package com.burim.order_service.event;

import com.burim.order_service.service.OrderEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderStatusChangedEventListenerTest {

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderStatusChangedEventListener listener;

    @Test
    void handle_ShouldDelegateToKafkaProducer() {
        var event = new OrderStatusChangedEvent(1L, "user-1", "PENDING", "CANCELLED");

        listener.handle(event);

        verify(orderEventProducer).sendOrderStatusChangedEvent(event);
    }

    @Test
    void handle_WhenKafkaProducerFails_ShouldNotPropagateException() {
        var event = new OrderStatusChangedEvent(1L, "user-1", "PENDING", "CANCELLED");
        doThrow(new RuntimeException("Kafka is unavailable"))
                .when(orderEventProducer).sendOrderStatusChangedEvent(event);

        assertThatCode(() -> listener.handle(event)).doesNotThrowAnyException();
    }
}
