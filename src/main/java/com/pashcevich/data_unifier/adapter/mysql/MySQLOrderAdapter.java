package com.pashcevich.data_unifier.adapter.mysql;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import com.pashcevich.data_unifier.adapter.mysql.repository.OrderRepository;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MySQLOrderAdapter {

    private final OrderRepository orderRepository;

    public List<UnifiedOrderDto> getAll() {
        try {
            return convertToUnified(fetchAllData());
        } catch (Exception e) {
            log.error("Failed to fetch all orders", e);
            throw new DataUnificationException("Failed to fetch orders", e);
        }
    }

    protected List<OrderEntity> fetchAllData() throws Exception {
        return orderRepository.findAll();
    }

    protected Optional<OrderEntity> fetchById(Long id) throws Exception {
        return orderRepository.findById(id);
    }

    protected String getAdapterName() {
        return "MySQL";
    }

    protected List<UnifiedOrderDto> convertToUnified(List<OrderEntity> orderEntities) {
        return orderEntities.stream()
                .map(this::convertSingleToUnified)
                .collect(Collectors.toList());
    }

    protected UnifiedOrderDto convertSingleToUnified(OrderEntity orderEntity) {
        if (orderEntity == null) {
            return null;
        }

        return UnifiedOrderDto.builder()
                .id(orderEntity.getId())
                .orderId(orderEntity.getUserId())
                .status(orderEntity.getStatus())
                .createdAt(orderEntity.getCreatedAt())
                .totalAmount(orderEntity.getTotalAmount())
                .build();
    }
}