package com.global.worker.infrastructure.mongo;

import com.global.worker.domain.model.Order;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderMongoAdapterTest {
    @Test
    void save_order_success() {
        ReactiveMongoRepositorySpring repo = mock(ReactiveMongoRepositorySpring.class);
        OrderMongoAdapter adapter = new OrderMongoAdapter(repo);
        Order order = new Order(new ObjectId(), "order-1", "customer-1", null);
        when(repo.insert(any(Order.class))).thenReturn(Mono.just(order));

        StepVerifier.create(adapter.save(order))
                .expectNext(order)
                .verifyComplete();
    }
}
