package com.pashcevich.data_unifier.adapter.kafka.producer.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedCustomerDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String type;
    private Instant timestamp; // ← был LocalDateTime, теперь Instant
}