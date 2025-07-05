package com.global.worker.domain.port.input;

import com.global.worker.domain.model.OrderMessage;

import reactor.core.publisher.Mono;

public interface OrderProcessorPort {
    Mono<Void> process(OrderMessage orderMessage);
}
