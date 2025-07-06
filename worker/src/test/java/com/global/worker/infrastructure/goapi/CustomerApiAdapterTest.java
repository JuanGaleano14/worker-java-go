package com.global.worker.infrastructure.goapi;

import com.global.worker.domain.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomerApiAdapterTest {
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void getCustomer_success() {
        WebClient client = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec uriSpec = (WebClient.RequestHeadersUriSpec) mock(
                WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = (WebClient.RequestHeadersSpec) mock(
                WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Customer expected = new Customer("customer-1", "Ana", "ana@mail.com", true);
        // Mock chain
        when(client.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Object[].class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Customer.class)).thenReturn(Mono.just(expected));
        CustomerApiAdapter adapter = new CustomerApiAdapter(client);
        StepVerifier.create(adapter.getCustomer("customer-1"))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void getCustomer_notFound() {
        WebClient client = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec uriSpec = (WebClient.RequestHeadersUriSpec) mock(
                WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = (WebClient.RequestHeadersSpec) mock(
                WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        // Mock chain
        when(client.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Object[].class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Customer.class))
                .thenReturn(Mono.error(new RuntimeException("Cliente no encontrado: customer-2")));
        CustomerApiAdapter adapter = new CustomerApiAdapter(client);
        StepVerifier.create(adapter.getCustomer("customer-2"))
                .expectErrorMatches(e -> e.getMessage().contains("Cliente no encontrado"))
                .verify();
    }
}
