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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private JsonUtil jsonUtil;

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
                    if (Boolean.FALSE.equals(acquired)) {
                        log.warn("Pedido ya está siendo procesado: {} (lock no liberado o reintento muy rápido)",
                                msg.getOrderId());
                        // 2 segundos antes de reintentar para liberar el lock
                        return Mono.delay(Duration.ofSeconds(2))
                                .then(retryHandler.releaseLock(msg.getOrderId())
                                        .doOnSuccess(v -> log.info(
                                                "Lock liberado para pedido: {} (por fallo de adquisición)",
                                                msg.getOrderId()))
                                        .doOnError(e -> log.error("Error liberando lock para pedido {}: {}",
                                                msg.getOrderId(), e.getMessage()))
                                        .then(handleRetry(msg, new RuntimeException(
                                                "No se pudo adquirir el lock para el pedido: " + msg.getOrderId()))));
                    }

                    return customerService.getCustomer(msg.getCustomerId())
                            .filter(Customer::isActive)
                            .switchIfEmpty(Mono.error(new IllegalStateException("Cliente inactivo o no encontrado")))
                            .zipWith(
                                    Flux.fromIterable(msg.getProducts())
                                            .flatMap(id -> productService.getProduct(id)
                                                    .onErrorResume(e -> {
                                                        log.warn("Error al obtener producto {}: {}", id,
                                                                e.getMessage());
                                                        // Devolver un producto nulo para evitar cancelación
                                                        return Mono
                                                                .just(new Product(id, "ERROR", "No disponible", 0.0));
                                                    }))
                                            .collectList())
                            .flatMap(tuple -> {
                                List<Product> enrichedProducts = tuple.getT2();
                                // Si algún producto es de error, lanzar excepción controlada
                                if (enrichedProducts.stream().anyMatch(p -> "ERROR".equals(p.getName()))) {
                                    String ids = enrichedProducts.stream()
                                            .filter(p -> "ERROR".equals(p.getName()))
                                            .map(Product::getProductId)
                                            .reduce((a, b) -> a + ", " + b).orElse("");
                                    return Mono.error(new RuntimeException("Productos no encontrados: " + ids));
                                }
                                Order enriched = new Order(
                                        null,
                                        msg.getOrderId(),
                                        msg.getCustomerId(),
                                        enrichedProducts);
                                return orderRepository.save(enriched)
                                        .doOnSuccess(o -> log.info("Pedido procesado: {}", o.getOrderId()))
                                        .then();
                            })
                            .onErrorResume(error -> retryHandler.releaseLock(msg.getOrderId())
                                    .doOnSuccess(v -> log.info("Lock liberado para pedido: {} (por error de consumo)",
                                            msg.getOrderId()))
                                    .doOnError(e -> log.error("Error liberando lock para pedido {}: {}",
                                            msg.getOrderId(), e.getMessage()))
                                    .then(handleRetry(msg, error)))
                            .doFinally(sig -> retryHandler.releaseLock(msg.getOrderId())
                                    .doOnSuccess(v -> log.info("Lock liberado para pedido: {}", msg.getOrderId()))
                                    .doOnError(e -> log.error("Error liberando lock para pedido {}: {}",
                                            msg.getOrderId(), e.getMessage()))
                                    .subscribe());
                });
    }

    private Mono<Void> handleRetry(OrderMessage msg, Throwable error) {
        log.warn("Error procesando pedido {}: {}", msg.getOrderId(), error.getMessage());

        return retryHandler.incrementRetry(msg.getOrderId())
                .flatMap(retries -> {
                    if (retries < MAX_RETRIES) {
                        long delaySeconds = (long) Math.pow(2, retries); // reintentos exponenciales
                        log.info("Reintentando en {}s (intento #{})...", delaySeconds, retries);
                        return Mono.delay(Duration.ofSeconds(delaySeconds))
                                .then(process(msg)); // Reintentar el procesamiento
                    } else {
                        log.error("Pedido {} alcanzó el máximo de reintentos", msg.getOrderId());
                        String json = jsonUtil.toJson(msg, error.getMessage(), retries);
                        return retryHandler.saveFailedOrder(msg.getOrderId(), json)
                                .then();
                    }
                });
    }
}
