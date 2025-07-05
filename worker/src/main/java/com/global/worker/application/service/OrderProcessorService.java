package com.global.worker.application.service;

import com.global.worker.domain.model.Customer;
import com.global.worker.domain.model.Order;
import com.global.worker.domain.model.OrderMessage;
import com.global.worker.domain.model.Product;
import com.global.worker.domain.port.input.OrderProcessorPort;
import com.global.worker.domain.port.output.CustomerServicePort;
import com.global.worker.domain.port.output.OrderRepositoryPort;
import com.global.worker.domain.port.output.ProductServicePort;
import com.global.worker.domain.port.output.RetryHandlerPort;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class OrderProcessorService implements OrderProcessorPort {

    private final CustomerServicePort customerService;
    private final ProductServicePort productService;
    private final OrderRepositoryPort orderRepository;
    private final RetryHandlerPort retryHandler;

    private static final int MAX_RETRIES = 5;

    public OrderProcessorService(
            CustomerServicePort customerService,
            ProductServicePort productService,
            OrderRepositoryPort orderRepository,
            RetryHandlerPort retryHandler) {
        this.customerService = customerService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.retryHandler = retryHandler;
    }

    @Override
    public Mono<Void> process(OrderMessage msg) {
        return retryHandler.acquireLock(msg.getOrderId())
                .flatMap(acquired -> {
                    if (!acquired) {
                        log.warn("Pedido ya está siendo procesado: {}", msg.getOrderId());
                        return Mono.empty();
                    }

                    return customerService.getCustomer(msg.getCustomerId())
                            .filter(Customer::isActive)
                            .switchIfEmpty(Mono.error(new IllegalStateException("Cliente inactivo o no encontrado")))
                            .zipWith(
                                    Flux.fromIterable(msg.getProducts())
                                            .flatMap(productService::getProduct)
                                            .collectList())
                            .flatMap(tuple -> {
                                List<Product> enrichedProducts = tuple.getT2();
                                Order enriched = new Order(null, msg.getOrderId(), msg.getCustomerId(), enrichedProducts);
                                return orderRepository.save(enriched)
                                        .doOnSuccess(o -> log.info("Pedido procesado: {}", o.getOrderId()))
                                        .then();
                            })
                            .onErrorResume(error -> handleRetry(msg, error))
                            .doFinally(sig -> retryHandler.releaseLock(msg.getOrderId()).subscribe());
                });
    }

    private Mono<Void> handleRetry(OrderMessage msg, Throwable error) {
        log.warn("Error procesando pedido {}: {}", msg.getOrderId(), error.getMessage());

        return retryHandler.incrementRetry(msg.getOrderId())
                .flatMap(retries -> {
                    if (retries < MAX_RETRIES) {
                        long delaySeconds = (long) Math.pow(2, retries); // backoff exponencial
                        log.info("Reintentando en {}s (intento #{})...", delaySeconds, retries);

                        return Mono.delay(Duration.ofSeconds(delaySeconds)).then();
                    } else {
                        log.error("Pedido {} alcanzó el máximo de reintentos", msg.getOrderId());
                        return Mono.empty();
                    }
                });
    }
}
