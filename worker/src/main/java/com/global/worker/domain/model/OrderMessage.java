package com.global.worker.domain.model;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage { 
    private String orderId;
    private String customerId;
    private List<String> products;
}
