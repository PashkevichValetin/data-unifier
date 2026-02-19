package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.metrics.ProcessingMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.KafkaException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaSenderService {

    private final UnifiedDataProducer unifiedDataProducer;
    private final ProcessingMetrics processingMetrics;

    private static final int BATCH_SIZE = 100;

    @Retryable(
            value = {KafkaException.class, ConnectException.class, TimeoutException.class},
            exclude = {DataIntegrityViolationException.class, IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public void sendUsersToKafka(List<UnifiedCustomerDto> users) {
        if (users == null || users.isEmpty()) {
            log.debug("No users to send");
            return;
        }

        log.info("Sending {} users to Kafka in batches of {}", users.size(), BATCH_SIZE);

        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < users.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, users.size());
            List<UnifiedCustomerDto> batch = users.subList(i, end);

            for (UnifiedCustomerDto user : batch) {
                try {
                    unifiedDataProducer.sendCustomer(user);
                    processingMetrics.incrementProcessed();
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    log.error("Failed to send user {}: {}", user.getId(), e.getMessage());
                }
            }
        }

        log.info("Sent {}/{} users successfully, {} failed", successCount, users.size(), errorCount);

        if (errorCount > 0 && successCount == 0) {
            throw new KafkaException("Failed to send all users to Kafka");
        }
    }

    @Retryable(
            value = {KafkaException.class, ConnectException.class, TimeoutException.class},
            exclude = {DataIntegrityViolationException.class, IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public void sendOrdersToKafka(List<UnifiedOrderDto> orders) {
        if (orders == null || orders.isEmpty()) {
            log.debug("No orders to send");
            return;
        }

        log.info("Sending {} orders to Kafka", orders.size());

        int successCount = 0;
        int errorCount = 0;

        for (UnifiedOrderDto order : orders) {
            try {
                unifiedDataProducer.sendOrder(order);
                processingMetrics.incrementProcessed();
                successCount++;
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to send order {}: {}", order.getId(), e.getMessage());
            }
        }

        log.info("Sent {}/{} orders successfully, {} failed", successCount, orders.size(), errorCount);

        if (errorCount > 0 && successCount == 0) {
            throw new KafkaException("Failed to send all orders to Kafka");
        }
    }

    @Retryable(
            value = {KafkaException.class, ConnectException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendSingleUser(UnifiedCustomerDto user) {
        try {
            unifiedDataProducer.sendCustomer(user);
            log.debug("Successfully sent single user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to send single user: {}", user.getId(), e);
            throw e;
        }
    }
}