package com.pashcevich.data_unifier.adapter.kafka.producer.dto;

import lombok.*;
import org.springframework.data.domain.jaxb.SpringDataJaxb;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedCustomerDto {
    private Long id;
    private Long userId;
    private String name;
    private String type;
    private String email;
    private LocalDateTime registrationDate;
    private List<UnifiedOrderDto> orders;
}