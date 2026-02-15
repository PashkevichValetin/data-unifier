package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import com.pashcevich.data_unifier.metrics.ProcessingMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaSenderService {

    private final UnifiedDataProducer unifiedDataProducer;
    private final ProcessingMetrics processingMetrics;

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @org.springframework.retry.annotation.Backoff(delay = 1000, multiplier = 2)
    )
    public void sendUsersToKafka(List<UnifiedCustomerDto> users) {
        log.info("Sending {} users to Kafka", users.size());
        sendBatch(users, unifiedDataProducer::sendCustomer, "user");
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @org.springframework.retry.annotation.Backoff(delay = 1000, multiplier = 2)
    )
    public void sendOrdersToKafka(List<UnifiedOrderDto> orders) {
        log.info("Sending {} orders to Kafka", orders.size());
        sendBatch(orders, unifiedDataProducer::sendOrder, "order");
    }

    private <T> void sendBatch(List<T> items, java.util.function.Consumer<T> sendFn, String type) {
        for (T item : items) {
            try {
                sendFn.accept(item);
                processingMetrics.incrementProcessed();
                log.debug("Successfully sent {}: {}", type, extractId(item));
            } catch (Exception e) {
                log.error("Failed to send {}: {}, error: {}", type, extractId(item), e.getMessage(), e);
            }
        }
    }

    private String extractId(Object item) {
        if (item instanceof UnifiedCustomerDto) {
            return ((UnifiedCustomerDto) item).getId().toString();
        } else if (item instanceof UnifiedOrderDto) {
            return ((UnifiedOrderDto) item).getId().toString();
        }
        return "unknown";
    }
}