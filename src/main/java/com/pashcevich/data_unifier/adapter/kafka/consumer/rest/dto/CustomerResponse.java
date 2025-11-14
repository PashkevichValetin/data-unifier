package com.pashcevich.data_unifier.adapter.kafka.consumer.rest.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerResponse {
    private Long userId;
    private String name;
    private String email;
    private LocalDateTime registrationDate;
    private int orderCount;
    private BigDecimal totalSpent;
}
