package com.global.worker.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RedisRetryHandlerAdapterTest {
    private ReactiveStringRedisTemplate redis;
    private RedisRetryHandlerAdapter adapter;

    @BeforeEach
    void setUp() {
        redis = mock(ReactiveStringRedisTemplate.class, RETURNS_DEEP_STUBS);
        adapter = new RedisRetryHandlerAdapter(redis);
    }

    @Test
    void acquireLock_success() {
        when(redis.opsForValue().setIfAbsent(any(), any(), any(Duration.class))).thenReturn(Mono.just(true));
        StepVerifier.create(adapter.acquireLock("order-1")).expectNext(true).verifyComplete();
    }

    @Test
    void releaseLock_success() {
        when(redis.delete(anyString())).thenReturn(Mono.just(1L));
        StepVerifier.create(adapter.releaseLock("order-1")).verifyComplete();
    }

    @Test
    void incrementRetry_success() {
        when(redis.opsForValue().increment(any())).thenReturn(Mono.just(2L));
        StepVerifier.create(adapter.incrementRetry("order-1")).expectNext(2L).verifyComplete();
    }

    @Test
    void saveFailedOrder_success() {
        when(redis.opsForValue().set(eq("failed:order-1"), eq("json-data"))).thenReturn(Mono.just(true));
        StepVerifier.create(adapter.saveFailedOrder("order-1", "json-data"))
                .verifyComplete();
        verify(redis.opsForValue()).set("failed:order-1", "json-data");
    }
}
