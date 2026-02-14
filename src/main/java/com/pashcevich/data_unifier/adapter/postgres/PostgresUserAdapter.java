package com.pashcevich.data_unifier.adapter.postgres;

import com.pashcevich.data_unifier.adapter.BaseDateAdapter;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PostgresUserAdapter extends BaseDateAdapter<UserEntity> {


    private final UserRepository userRepository;

    public PostgresUserAdapter(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    @Override
    protected List<UserEntity> fetchAllData() throws Exception {
        return userRepository.findAll();
    }

    @Override
    protected Optional<UserEntity> fetchById(Long id) throws Exception {
        return userRepository.findById(id);
    }

    @Override
    protected String getAdapterName() {
        return "PostgreSQL";
    }

    @Override
    protected List<UnifiedCustomerDto> convertToUnified(List<UserEntity> users) {
        return users.stream()
                .map(this::convertToUnifiedCustomer)
                .collect(Collectors.toList());
    }

    @Override
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