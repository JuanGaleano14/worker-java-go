package com.global.worker.infrastructure.kafka;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.global.worker.domain.model.OrderMessage;
import com.global.worker.domain.port.input.OrderProcessorPort;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test-orders")
public class FakeOrderController {

    private final OrderProcessorPort processor;

    public FakeOrderController(OrderProcessorPort processor) {
        this.processor = processor;
    }

    @PostMapping
    public Mono<Void> simulate(@RequestBody OrderMessage order) {
        return processor.process(order);
    }
}
