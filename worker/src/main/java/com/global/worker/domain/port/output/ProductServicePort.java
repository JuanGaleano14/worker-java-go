package com.global.worker.domain.port.output;

import com.global.worker.domain.model.Product;

import reactor.core.publisher.Mono;

public interface ProductServicePort {
    Mono<Product> getProduct(String id);
}
