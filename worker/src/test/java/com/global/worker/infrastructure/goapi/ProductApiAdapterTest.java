package com.global.worker.infrastructure.goapi;

import com.global.worker.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProductApiAdapterTest {
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void getProduct_success() {
        WebClient client = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec uriSpec = (WebClient.RequestHeadersUriSpec) mock(
                WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = (WebClient.RequestHeadersSpec) mock(
                WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Product expected = new Product("product-1", "Laptop", "desc", 1000);
        when(client.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Object[].class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Product.class)).thenReturn(Mono.just(expected));
        ProductApiAdapter adapter = new ProductApiAdapter(client);
        StepVerifier.create(adapter.getProduct("product-1"))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void getProduct_notFound() {
        WebClient client = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec uriSpec = (WebClient.RequestHeadersUriSpec) mock(
                WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = (WebClient.RequestHeadersSpec) mock(
                WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(client.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Object[].class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Product.class))
                .thenReturn(Mono.error(new RuntimeException("Producto no encontrado: product-2")));
        ProductApiAdapter adapter = new ProductApiAdapter(client);
        StepVerifier.create(adapter.getProduct("product-2"))
                .expectErrorMatches(e -> e.getMessage().contains("Producto no encontrado"))
                .verify();
    }
}
