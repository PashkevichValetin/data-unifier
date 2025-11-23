package com.pashcevich.data_unifier.adapter.kafka.producer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedCustomerDto {
    private String customerId;
    private Long userId;
    private String name;
    private String email;
    private LocalDateTime registrationDate;
    private List<OrderDto> orders = new ArrayList<>();
    private String dataSource;
    private LocalDateTime unifiedTimestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDto {
        private String orderId;
        private BigDecimal amount;
        private String status;
        private LocalDateTime orderDate;

    }

    public static UnifiedCustomerDto create(String customerId, Long userId, String name,
                                            String email, String dataSource) {
        UnifiedCustomerDto dto = new UnifiedCustomerDto();
        dto.setCustomerId(customerId);
        dto.setUserId(userId);
        dto.setName(name);
        dto.setEmail(email);
        dto.setDataSource(dataSource);
        dto.setUnifiedTimestamp(LocalDateTime.now());
        dto.setOrders(new ArrayList<>());
        return dto;
    }
}