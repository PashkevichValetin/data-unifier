package com.pashcevich.data_unifier.adapter.postgres;



import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresUserAdapter {

    private final UserRepository userRepository;

    public List<UnifiedCustomerDto> fetchUsersForUnification() {
        try {
            List<UserEntity> users = userRepository.findAll();
            log.info("Fetched {} users from PostgreSQL", users.size());

            return users.stream()
                    .map(this::convertToUnifiedDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching users from PostgreSQL", e);
            return List.of();
        }
    }

    private UnifiedCustomerDto convertToUnifiedDto(UserEntity user) {
        if (user == null) {
            return null;
        }

        UnifiedCustomerDto dto = new UnifiedCustomerDto();
        dto.setCustomerId("PG_" + user.getId());
        dto.setUserId(user.getId());
        dto.setName(user.getName() != null ? user.getName().trim() : "Unknown");
        dto.setEmail(user.getEmail() != null ? user.getEmail().trim().toLowerCase() : null);
        dto.setRegistrationDate(user.getRegistrationDate());
        dto.setDataSource("postgresql");
        dto.setUnifiedTimestamp(java.time.LocalDateTime.now());

        dto.setOrders(List.of());

        return dto;
    }
}