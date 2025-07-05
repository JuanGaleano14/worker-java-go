package com.global.worker.infrastructure.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.global.worker.domain.model.OrderMessage;
import com.global.worker.domain.port.input.OrderProcessorPort;

@Component
public class OrderKafkaListener {

    private final OrderProcessorPort processor;

    public OrderKafkaListener(OrderProcessorPort processor) {
        this.processor = processor;
    }

    @KafkaListener(topics = "orders", groupId = "order-group")
    public void listen(OrderMessage message) {
        processor.process(message).subscribe();
    }
}
