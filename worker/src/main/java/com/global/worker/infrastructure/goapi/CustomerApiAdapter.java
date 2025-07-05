package com.global.worker.infrastructure.goapi;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.global.worker.domain.model.Customer;
import com.global.worker.domain.port.output.CustomerServicePort;

import reactor.core.publisher.Mono;

@Component
public class CustomerApiAdapter implements CustomerServicePort {

    private final WebClient client;

    public CustomerApiAdapter(WebClient goApiClient) {
        this.client = goApiClient;
    }

    @Override
    public Mono<Customer> getCustomer(String id) {
        return client.get()
                .uri("/customers/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new RuntimeException("Cliente no encontrado: " + id)))
                .bodyToMono(Customer.class);
    }
}
