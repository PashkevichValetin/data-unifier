package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.postgres.PostgresUserAdapter;
import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataUnificationServiceImpl implements DataUnificationService {

    private final PostgresUserAdapter postgresUserAdapter;
    private final MySQLOrderAdapter mySQLOrderAdapter;
    private final UnifiedDataProducer unifiedDataProducer;
    private final ProcessingMetrics processingMetrics;

    @Override
    @Transactional
    public void processAllData() {
        log.info("Starting complete data processing");
        try {
            processUserData();
            processOrderData();
            log.info("Complete data processing finished successfully");
            processingMetrics.incrementProcessed();
        } catch (Exception e) {
            log.error("Failed to process all data", e);
            throw new DataUnificationException("Failed to process all data", e);
        }
    }

    @Override
    @Transactional
    public void processUserData() {
        log.info("Starting user data processing");
        try {
            List<UnifiedCustomerDto> users = postgresUserAdapter.getAll();
            sendToKafka(users, "user");
        } catch (Exception e) {
            throw new DataUnificationException("Failed to process user data", e);
        }
    }

    @Override
    @Transactional
    public void processOrderData() {
        log.info("Starting order data processing");
        try {
            List<UnifiedCustomerDto> orders = mySQLOrderAdapter.getAll();
            sendToKafka(orders, "order");
        } catch (Exception e) {
            throw new DataUnificationException("Failed to process order data", e);
        }
    }

    @Override
    @Transactional
    public void processUserById(Long userId) {
        log.info("Processing user by id: {}", userId);
        postgresUserAdapter.getById(userId)
                .ifPresentOrElse(
                        user -> {
                            unifiedDataProducer.send(user);
                            processingMetrics.incrementProcessed();
                            log.debug("Successful processed user: {}", userId);
                        },
                        () -> {
                            throw new DataUnificationException("User with id " + userId +
                                    " not found");
                        }
                );
    }

    @Override
    @Transactional
    public long getProcessedCount() {
        return processingMetrics.getProcessedCount();
    }

    private void sendToKafka(List<UnifiedCustomerDto> data, String dataType) {
        data.forEach(item -> {
            try {
                unifiedDataProducer.send(item);
                processingMetrics.incrementProcessed();
                log.debug("Successful sent {}: {}", dataType, item.getId());
            } catch (Exception e) {
                log.error("Failed to send {}: {}, error: {}", dataType, item.getId(), e.getMessage(), e);
            }
        });
    }

    @Component
    @RequiredArgsConstructor
    public static class ProcessingMetrics {
        private final AtomicLong processedCount = new AtomicLong(0);

        public void incrementProcessed() {
            processedCount.incrementAndGet();
        }

        public long getProcessedCount() {
            return processedCount.get();
        }

        public void reset() {
            processedCount.set(0);
        }
    }
}









