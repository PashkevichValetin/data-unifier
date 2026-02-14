package com.pashcevich.data_unifier.adapter.postgres;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import com.pashcevich.data_unifier.exception.UserAdapterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresUserAdapter {

    private final UserRepository userRepository;

    public List<UnifiedCustomerDto> getAll() {
        try {
            return convertToUnified(fetchAllData());
        } catch (Exception e) {
            log.error("Failed to fetch all users", e);
            throw new UserAdapterException("Failed to fetch users", e);
        }
    }

    public Optional<UnifiedOrderDto> getById(Long id) {
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
        return UnifiedCustomerDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .type("USER")
                .timestamp(Instant.now())
                .build();
    }

}