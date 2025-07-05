package com.global.worker.domain.port.output;

import com.global.worker.domain.model.Order;

import reactor.core.publisher.Mono;

public interface OrderRepositoryPort {
    Mono<Order> save(Order order);
}
