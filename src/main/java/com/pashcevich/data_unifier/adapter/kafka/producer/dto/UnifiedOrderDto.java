package com.pashcevich.data_unifier.adapter.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnifiedOrderDto {

    private Long id;
    private Long orderId;
    private String status;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
}
