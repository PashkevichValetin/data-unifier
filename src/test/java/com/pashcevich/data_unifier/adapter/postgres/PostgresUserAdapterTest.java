package com.pashcevich.data_unifier.adapter.postgres;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostgresUserAdapterTest {

    @Mock
    private UserRepository userRepository;

    private PostgresUserAdapter postgresUserAdapter;

    @BeforeEach
    void setUp() {
        postgresUserAdapter = new PostgresUserAdapter(userRepository);
    }

    @Test
    void getAllUserForUnification_shouldReturnListOfUnificationCustomerDTOS() {
        // GIVEN
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setName("John Doe");
        user1.setEmail("john@example.com");
        user1.setRegistrationDate(LocalDateTime.now());

        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane@example.com");
        user2.setRegistrationDate(LocalDateTime.now());

        List<UserEntity> users = Arrays.asList(user1, user2);

        // WHEN
        when(userRepository.findAll()).thenReturn(users);

        List<UnifiedCustomerDto> result = postgresUserAdapter.getAllUserForUnification();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");

        assertThat(result.get(1).getUserId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Jane Smith");
        assertThat(result.get(1).getEmail()).isEqualTo("jane@example.com");

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_withValidId_shouldReturnUser() {
        // GIVEN
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setName("John Doe");
        userEntity.setEmail("john@example.com");
        userEntity.setRegistrationDate(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        // WHEN
        Optional<UnifiedCustomerDto> result = postgresUserAdapter.getUserById("1");

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_withNullId_shouldReturnEmptyOptional() {
        // WHEN
        Optional<UnifiedCustomerDto> result = postgresUserAdapter.getUserById(null);

        // THEN
        assertThat(result).isEmpty();
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserById_withEmptyId_shouldReturnEmptyOptional() {
        // WHEN
        Optional<UnifiedCustomerDto> result = postgresUserAdapter.getUserById("   ");

        // THEN
        assertThat(result).isEmpty();
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserById_withInvalidIdFormat_shouldReturnEmptyOptional() {
        // WHEN
        Optional<UnifiedCustomerDto> result = postgresUserAdapter.getUserById("invalid");

        // THEN
        assertThat(result).isEmpty();
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserById_withNonExistentId_shouldReturnEmptyOptional() {
        // GIVEN
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // WHEN
        Optional<UnifiedCustomerDto> result = postgresUserAdapter.getUserById("1");

        // THEN
        assertThat(result).isEmpty();
        verify(userRepository).findById(1L);
    }

    @Test
    void getAllUserForUnification_withEmptyRepository_shouldReturnEmptyList() {
        // GIVEN
        when(userRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<UnifiedCustomerDto> result = postgresUserAdapter.getAllUserForUnification();

        // THEN
        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void getAllUserForUnification_withException_shouldThrowRuntimeException() {
        // GIVEN
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThatThrownBy(() -> postgresUserAdapter.getAllUserForUnification())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        verify(userRepository).findAll();
    }
}
























































