package com.global.worker.infrastructure.goapi;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.global.worker.domain.model.Product;
import com.global.worker.domain.port.output.ProductServicePort;

import reactor.core.publisher.Mono;

@Component
public class ProductApiAdapter implements ProductServicePort {

    private final WebClient client;

    public ProductApiAdapter(WebClient goApiClient) {
        this.client = goApiClient;
    }

    @Override
    public Mono<Product> getProduct(String id) {
        return client.get()
                .uri("/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new RuntimeException("Producto no encontrado: " + id)))
                .bodyToMono(Product.class);
    }
}
