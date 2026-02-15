package com.pashcevich.data_unifier.adapter.postgres;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import com.pashcevich.data_unifier.adapter.mysql.repository.OrderRepository;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import com.pashcevich.data_unifier.exception.UserAdapterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresUserAdapter {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository; // ✅ теперь внедрено

    public List<UnifiedCustomerDto> getAll() {
        try {
            return convertToUnified(fetchAllData());
        } catch (Exception e) {
            log.error("Failed to fetch all users", e);
            throw new UserAdapterException("Failed to fetch users", e);
        }
    }

    public Optional<UnifiedCustomerDto> getById(Long id) {
        try {
            return fetchById(id)
                    .map(this::convertSingleToUnified);
        } catch (Exception e) {
            log.error("Failed to fetch user by id: {}", id, e);
            throw new DataUnificationException("Failed to fetch user by id: " + id, e);
        }
    }

    protected List<UserEntity> fetchAllData() throws Exception {
        return userRepository.findAll();
    }

    protected Optional<UserEntity> fetchById(Long id) throws Exception {
        return userRepository.findById(id);
    }

    protected String getAdapterName() {
        return "PostgreSQL";
    }

    protected List<UnifiedCustomerDto> convertToUnified(List<UserEntity> users) {
        return users.stream()
                .map(this::convertToUnifiedCustomer)
                .collect(Collectors.toList());
    }

    protected UnifiedCustomerDto convertSingleToUnified(UserEntity user) {
        return convertToUnifiedCustomer(user);
    }

    private UnifiedCustomerDto convertToUnifiedCustomer(UserEntity user) {
        List<UnifiedOrderDto> orders = fetchOrdersForUser(user.getId());

        return UnifiedCustomerDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .type("USER")
                .orders(orders)
                .registrationDate(LocalDateTime.now())
                .build();
    }

    private List<UnifiedOrderDto> fetchOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::convertToUnifiedOrder)
                .collect(Collectors.toList());
    }

    private UnifiedOrderDto convertToUnifiedOrder(OrderEntity order) {
        return UnifiedOrderDto.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}