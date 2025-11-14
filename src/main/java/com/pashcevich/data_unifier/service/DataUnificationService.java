package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.postgres.PostgresUserAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataUnificationService {

    private final PostgresUserAdapter postgresUserAdapter;
    private final MySQLOrderAdapter mySQLOrderAdapter;
    private final UnifiedDataProducer unifiedDataProducer;

    @Scheduled(fixedRate = 30000)
    public void unifyAndSendData() {
        log.info("Starting data unification process...");

        try {
            List<UnifiedCustomerDto> users = postgresUserAdapter.getAllUserForUnification();
            log.info("Found {} users for unification", users.size());

            users.forEach(user -> {
                List<UnifiedCustomerDto.OrderData> orders = mySQLOrderAdapter.getOrdersByUserId(
                        user.getUserId());
                user.setOrders(orders);
                log.info("User {} has {} orders", user.getUserId(), orders.size());
            });
            users.forEach(unifiedDataProducer::sendUnifiedCustomer);
            log.info("Successfully processed {} unified customer records", users.size());
        } catch (Exception e) {
            log.error("Error during data unification process", e);
        }
    }

}
