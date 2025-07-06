package com.global.worker.infrastructure.kafka;

import com.global.worker.domain.model.OrderMessage;
import com.global.worker.domain.port.input.OrderProcessorPort;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class OrderKafkaListenerTest {
    @Test
    void listen_shouldProcessOrder() {
        OrderProcessorPort processor = mock(OrderProcessorPort.class);
        OrderKafkaListener listener = new OrderKafkaListener(processor);
        OrderMessage msg = new OrderMessage("order-1", "customer-1", java.util.List.of("product-1"));
        when(processor.process(msg)).thenReturn(Mono.empty());

        listener.listen(msg);
        verify(processor).process(msg);
    }
}
