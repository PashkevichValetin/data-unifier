package com.pashcevich.data_unifier.adapter.mysql;

import com.pashcevich.data_unifier.adapter.BaseDateAdapter;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import com.pashcevich.data_unifier.adapter.mysql.repository.OrderRepository;
import com.pashcevich.data_unifier.exception.AdapterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class MySQLOrderAdapter extends BaseDateAdapter<OrderEntity> {

    private final OrderRepository orderRepository;

    public MySQLOrderAdapter(OrderRepository orderRepository) {
        super();
        this.orderRepository = orderRepository;
    }

    @Override
    protected List<OrderEntity> fetchAllData() throws Exception {
        return orderRepository.findAll();
    }

    @Override
    protected Optional<OrderEntity> fetchById(Long id) throws Exception {
        return orderRepository.findById(id);
    }

    @Override
    protected String getAdapterName() {
        return "MySQL";
    }

    @Override
    protected List<UnifiedCustomerDto> convertToUnified(List<OrderEntity> order) {
        return List.of();
    }

    @Override
    protected UnifiedCustomerDto convertSingleToUnified(OrderEntity order) {
        return null;
    }
}