package com.pashcevich.data_unifier.adapter.postgres;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostgresUserAdapter {

    private final UserRepository userRepository;

    public List<UnifiedCustomerDto> getAllUserForUnification() {
        List<UserEntity> users = userRepository.findAll();

        return users.stream()
                .map(this::convertToUnifiedDto)
                .toList();
    }

    private UnifiedCustomerDto convertToUnifiedDto(UserEntity user) {
        UnifiedCustomerDto dto = new UnifiedCustomerDto();
        dto.setUserId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRegistrationDate(user.getRegistrationDate());

        return dto;
    }

}
