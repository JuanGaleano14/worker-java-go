package com.global.worker.application.service;

import com.global.worker.domain.model.Customer;
import com.global.worker.domain.model.Order;
import com.global.worker.domain.model.OrderMessage;
import com.global.worker.domain.model.Product;
import com.global.worker.domain.port.output.CustomerServicePort;
import com.global.worker.domain.port.output.OrderRepositoryPort;
import com.global.worker.domain.port.output.ProductServicePort;
import com.global.worker.domain.port.output.RetryHandlerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderProcessorServiceTest {
    private CustomerServicePort customerService;
    private ProductServicePort productService;
    private OrderRepositoryPort orderRepository;
    private RetryHandlerPort retryHandler;
    private OrderProcessorService service;

    @BeforeEach
    void setUp() {
        customerService = mock(CustomerServicePort.class);
        productService = mock(ProductServicePort.class);
        orderRepository = mock(OrderRepositoryPort.class);
        retryHandler = mock(RetryHandlerPort.class);
        service = new OrderProcessorService(customerService, productService, orderRepository, retryHandler);

        java.lang.reflect.Field jsonUtilField;
        try {
            jsonUtilField = OrderProcessorService.class.getDeclaredField("jsonUtil");
            jsonUtilField.setAccessible(true);
            jsonUtilField.set(service, new JsonUtil());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void process_successfulOrder() {
        OrderMessage msg = new OrderMessage("order-1", "customer-1", Arrays.asList("product-1", "product-2"));
        Customer customer = new Customer("customer-1", "Ana", "ana@mail.com", true);
        Product product1 = new Product("product-1", "Laptop", "desc", 1000);
        Product product2 = new Product("product-2", "Mouse", "desc", 20);
        Order order = new Order(null, "order-1", "customer-1", Arrays.asList(product1, product2));

        when(retryHandler.acquireLock(any())).thenReturn(Mono.just(true));
        when(customerService.getCustomer("customer-1")).thenReturn(Mono.just(customer));
        when(productService.getProduct("product-1")).thenReturn(Mono.just(product1));
        when(productService.getProduct("product-2")).thenReturn(Mono.just(product2));
        when(orderRepository.save(any())).thenReturn(Mono.just(order));
        when(retryHandler.releaseLock(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.process(msg))
                .verifyComplete();

        verify(orderRepository).save(any());
        verify(retryHandler).acquireLock("order-1");
        verify(retryHandler).releaseLock("order-1");
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_inactiveCustomer_shouldErrorAndRetry() {
        OrderMessage msg = new OrderMessage("order-2", "customer-2", List.of("product-1"));
        Customer customer = new Customer("customer-2", "Pablo", "pablo@mail.com", false);

        when(retryHandler.acquireLock(any())).thenReturn(Mono.just(true));
        when(customerService.getCustomer("customer-2")).thenReturn(Mono.just(customer));

        // Simula 5 reintentos
        when(retryHandler.incrementRetry(any())).thenReturn(
                Mono.just(1L), Mono.just(2L), Mono.just(3L), Mono.just(4L), Mono.just(5L));
        when(retryHandler.releaseLock(any())).thenReturn(Mono.empty());
        when(retryHandler.saveFailedOrder(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.process(msg))
                .verifyComplete();

        verify(retryHandler, times(5)).incrementRetry("order-2");
        verify(retryHandler, atLeastOnce()).releaseLock("order-2");
        verify(retryHandler).saveFailedOrder(eq("order-2"), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_lockNotAcquired_shouldNotProcess() {
        OrderMessage msg = new OrderMessage("order-3", "customer-3", List.of("product-1"));
        when(retryHandler.acquireLock(any())).thenReturn(Mono.just(false));
        when(retryHandler.releaseLock(any())).thenReturn(Mono.empty());

        // Simula 5 reintentos
        when(retryHandler.incrementRetry(any())).thenReturn(
                Mono.just(1L), Mono.just(2L), Mono.just(3L), Mono.just(4L), Mono.just(5L));
        when(retryHandler.saveFailedOrder(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.process(msg))
                .verifyComplete();

        verifyNoInteractions(customerService);
        verifyNoInteractions(productService);
        verifyNoInteractions(orderRepository);
    }
}
