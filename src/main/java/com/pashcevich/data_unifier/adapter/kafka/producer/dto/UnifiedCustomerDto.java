package com.pashcevich.data_unifier.adapter.kafka.producer.dto;

import lombok.*;

        import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedCustomerDto {
    private Long id;
    private String name;
    private String email;
    private String type;
    private Instant timestamp;

    public UnifiedCustomerDto(Long id, String name, String email, String type) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.type = type;
        this.timestamp = Instant.now();
    }

    @Override
    public String toString() {
        return "UnifiedCustomerDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
