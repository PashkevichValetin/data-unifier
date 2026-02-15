package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.postgres.PostgresUserAdapter;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import com.pashcevich.data_unifier.metrics.ProcessingMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataUnificationServiceImpl implements DataUnificationService {

    private final PostgresUserAdapter postgresUserAdapter;
    private final MySQLOrderAdapter mySQLOrderAdapter;
    private final UnifiedDataProducer unifiedDataProducer;
    private final ProcessingMetrics processingMetrics;
    private final TransactionTemplate transactionTemplate;
    private final KafkaSenderService kafkaSenderService;

    @Override
    public void processAllData() {
        log.info("Starting complete data processing");

        try {
            transactionTemplate.execute(status -> {
                processUserData();
                processOrderData();
                return null;
            });
            log.info("Complete data processing finished successfully");
            processingMetrics.incrementProcessed();
        } catch (Exception e) {
            log.error("Failed to process all data", e);
            throw new DataUnificationException("Failed to process all data", e);
        }
    }

    @Override
    public void processUserData() {
        log.info("Starting user data processing");
        try {
            List<UnifiedCustomerDto> users = postgresUserAdapter.getAll();
            kafkaSenderService.sendUsersToKafka(users);
        } catch (Exception e) {
            log.error("Failed to process user data", e);
            throw new DataUnificationException("Failed to process user data", e);
        }
    }

    @Override
    public void processOrderData() {
        log.info("Starting order data processing");
        try {
            List<UnifiedOrderDto> orders = mySQLOrderAdapter.getAll();
            kafkaSenderService.sendOrdersToKafka(orders);
        } catch (Exception e) {
            log.error("Failed to process order data", e);
            throw new DataUnificationException("Failed to process order data", e);
        }
    }

    @Override
    public void processUserById(Long userId) {
        log.info("Processing user by id: {}", userId);
        postgresUserAdapter.getById(userId)
                .ifPresentOrElse(
                        user -> {
                            try {
                                unifiedDataProducer.sendCustomer(user);
                                processingMetrics.incrementProcessed();
                                log.debug("Successfully processed user: {}", userId);
                            } catch (Exception e) {
                                log.error("Failed to send user {} to Kafka", userId, e);
                                throw new DataUnificationException("Failed to send user to Kafka", e);
                            }
                        },
                        () -> {
                            log.warn("User with id {} not found", userId);
                            throw new DataUnificationException("User with id " + userId + " not found");
                        }
                );
    }

    @Override
    public long getProcessedCount() {
        return processingMetrics.getProcessedCount();
    }
}