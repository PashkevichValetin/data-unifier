package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.postgres.PostgresUserAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataUnificationServiceTest {

    @Mock
    private PostgresUserAdapter postgresUserAdapter;

    @Mock
    private MySQLOrderAdapter mySQLOrderAdapter;

    @Mock
    private UnifiedDataProducer unifiedDataProducer;

    private DataUnificationService dataUnificationService;

    @BeforeEach
    void setUp() {
        dataUnificationService = new DataUnificationService(
                postgresUserAdapter,
                mySQLOrderAdapter,
                unifiedDataProducer
        );
    }

    @Test
    void unifyCustomerById_shouldReturnUnifiedCustomer() {
        //GIVEN
        Long userId = 1L;
        UnifiedCustomerDto user = new UnifiedCustomerDto();
        user.setUserId(userId);
        user.setEmail("test@example.com");

        List<UnifiedCustomerDto.OrderData> orders = List.of();

        when(postgresUserAdapter.getUserById("1")).thenReturn(Optional.of(user));
        when(mySQLOrderAdapter.getOrdersByUserId(userId)).thenReturn(orders);

        //WHEN
        UnifiedCustomerDto result = dataUnificationService.unifyCustomerById(userId);

        //THEN
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(postgresUserAdapter).getUserById("1");
        verify(mySQLOrderAdapter).getOrdersByUserId(userId);
        verify(unifiedDataProducer).sendUnifiedCustomer(result);
    }
}





