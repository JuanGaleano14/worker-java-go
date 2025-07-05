package com.global.worker.infrastructure.redis;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import com.global.worker.domain.port.output.RetryHandlerPort;

import reactor.core.publisher.Mono;

@Component
public class RedisRetryHandlerAdapter implements RetryHandlerPort {

    private final ReactiveStringRedisTemplate redis;

    public RedisRetryHandlerAdapter(ReactiveStringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Mono<Boolean> acquireLock(String orderId) {
        String key = "lock:" + orderId;
        return redis.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(5))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> releaseLock(String orderId) {
        return redis.delete("lock:" + orderId).then();
    }

    @Override
    public Mono<Long> incrementRetry(String orderId) {
        String key = "retry:" + orderId;
        return redis.opsForValue().increment(key);
    }
}
