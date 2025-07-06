package com.global.worker.domain.port.output;

import reactor.core.publisher.Mono;

public interface RetryHandlerPort {
    Mono<Boolean> acquireLock(String orderId);

    Mono<Void> releaseLock(String orderId);

    Mono<Long> incrementRetry(String orderId);

    Mono<Void> saveFailedOrder(String orderId, String json);
}