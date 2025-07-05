package com.global.worker.domain.port.output;

import com.global.worker.domain.model.Customer;

import reactor.core.publisher.Mono;

public interface CustomerServicePort {
    Mono<Customer> getCustomer(String id);
}
