package com.pashcevich.data_unifier.service; // исправлено: service, не sevice

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.postgres.PostgresUserAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataUnificationService {
    private final PostgresUserAdapter postgresUserAdapter;
    private final MySQLOrderAdapter mySQLOrderAdapter;
    private final UnifiedDataProducer unifiedDataProducer;

    public void unifyAllCustomers() {
        log.info("Starting data unification process...");

        try {
            List<UnifiedCustomerDto> users = postgresUserAdapter.getAllUserForUnification();
            log.info("Found {} users for unification", users.size());

            users.forEach(user -> {
                try {
                    List<UnifiedCustomerDto.OrderData> orders = mySQLOrderAdapter
                            .getOrdersByUserId(user.getUserId());
                    user.setOrders(orders);
                    log.debug("User ID {} has {} orders", user.getUserId(), orders.size());

                    unifiedDataProducer.sendUnifiedCustomer(user);
                } catch (Exception e) {
                    log.error("Failed to process orders for user ID {}: {}",
                            user.getUserId(), e.getMessage(), e);
                }
            });
            log.info("Data unification completed successfully. Processed {} users.", users.size());
        } catch (Exception e) {
            log.error("Data unification process failed: {}", e.getMessage(), e);
            throw new RuntimeException("Data unification failed", e);
        }
    }

    public UnifiedCustomerDto unifyCustomerById(Long userId) {
        log.info("Unifying data for user ID: {}", userId);

        // Здесь будет логика для получения одного пользователя
        // Пока просто заглушка
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}