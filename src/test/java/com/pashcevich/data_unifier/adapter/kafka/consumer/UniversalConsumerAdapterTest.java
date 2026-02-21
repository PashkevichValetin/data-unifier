package com.pashcevich.data_unifier.adapter.kafka.consumer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class UniversalConsumerAdapterTest {

    @InjectMocks
    private UniversalConsumerAdapter consumerAdapter;

    private UnifiedCustomerDto testCustomer;
    private UnifiedOrderDto testOrder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consumerAdapter, "unifiedCustomersTopic", "unified-customers-test");

        testOrder = UnifiedOrderDto.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .status("COMPLETED")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();

        testCustomer = UnifiedCustomerDto.builder()
                .id(1L)
                .userId(1L)
                .name("Test User")
                .email("test@example.com")
                .type("USER")
                .registrationDate(LocalDateTime.now())
                .orders(List.of(testOrder))
                .build();
    }

    @Test
    void consumeUnifiedCustomer_shouldProcessMessage() {
        // WHEN & THEN - не должно быть исключений
        assertThatCode(() -> consumerAdapter.consumeUnifiedCustomer(
                testCustomer,
                "1",
                0,
                "unified-customers-test",
                System.currentTimeMillis()
        )).doesNotThrowAnyException();
    }

    @Test
    void consumeUnifiedCustomer_withCustomerWithoutOrders_shouldProcessMessage() {
        // GIVEN
        UnifiedCustomerDto customerWithoutOrders = UnifiedCustomerDto.builder()
                .id(2L)
                .userId(2L)
                .name("Test User 2")
                .email("test2@example.com")
                .type("USER")
                .registrationDate(LocalDateTime.now())
                .orders(List.of())
                .build();

        // WHEN & THEN
        assertThatCode(() -> consumerAdapter.consumeUnifiedCustomer(
                customerWithoutOrders,
                "2",
                1,
                "unified-customers-test",
                System.currentTimeMillis()
        )).doesNotThrowAnyException();
    }

    @Test
    void consumeUnifiedCustomer_withNullCustomer_shouldNotThrowException() {
        // WHEN & THEN - теперь не падает благодаря проверке на null в адаптере
        assertThatCode(() -> consumerAdapter.consumeUnifiedCustomer(
                null,
                "3",
                2,
                "unified-customers-test",
                System.currentTimeMillis()
        )).doesNotThrowAnyException();
    }
}