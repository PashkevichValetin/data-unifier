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
            List<UnifiedCustomerDto> postgresCustomers = postgresUserAdapter.fetchUsersForUnification();
            log.info("Found {} customers from PostgreSQL", postgresCustomers.size());

            List<UnifiedCustomerDto> mysqlCustomers = mySQLOrderAdapter.fetchCustomersWithOrders();
            log.info("Found {} customers from MySQL with orders", mysqlCustomers.size());

            unifiedDataProducer.sendUnifiedCustomers(postgresCustomers);
            unifiedDataProducer.sendUnifiedCustomers(mysqlCustomers);

            log.info("Data unification completed. Sent {} total customer records",
                    postgresCustomers.size() + mysqlCustomers.size());

        } catch (Exception e) {
            log.error("Error during data unification process", e);
        }
    }
    public void triggerManualUnification() {
        log.info("Manual data unification triggered");
        unifyAndSendData();
    }
}