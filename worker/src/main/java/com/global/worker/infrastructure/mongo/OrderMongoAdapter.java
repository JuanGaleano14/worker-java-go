package com.global.worker.infrastructure.mongo;

import org.springframework.stereotype.Repository;

import com.global.worker.domain.model.Order;
import com.global.worker.domain.port.output.OrderRepositoryPort;

import reactor.core.publisher.Mono;

@Repository
public class OrderMongoAdapter implements OrderRepositoryPort {

    private final ReactiveMongoRepositorySpring repo;

    public OrderMongoAdapter(ReactiveMongoRepositorySpring repo) {
        this.repo = repo;
    }

    @Override
    public Mono<Order> save(Order order) {
        return repo.insert(order);
    }
}
