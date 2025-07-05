package com.global.worker.infrastructure.mongo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.global.worker.domain.model.Order;

public interface ReactiveMongoRepositorySpring extends ReactiveMongoRepository<Order, ObjectId> {
}
