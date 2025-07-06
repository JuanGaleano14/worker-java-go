package com.global.worker.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.global.worker.domain.model.OrderMessage;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(OrderMessage msg, String error, long retries) {
        try {
            return objectMapper.writeValueAsString(new FailedOrder(msg, error, retries));
        } catch (JsonProcessingException e) {
            return "{\"error\":\"Error serializando mensaje fallido\"}";
        }
    }

    public static class FailedOrder {
        public final OrderMessage order;
        public final String error;
        public final long retries;

        public FailedOrder(OrderMessage order, String error, long retries) {
            this.order = order;
            this.error = error;
            this.retries = retries;
        }
    }
}
