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
    public <T> void sendToKafka(List<T> data, String dataType) {
        log.info("Sending {} items of type {} to Kafka", data.size(), dataType);

        data.stream()
                .map(item -> {
                    try {
                        unifiedDataProducer.send((UnifiedCustomerDto) item);
                        processingMetrics.incrementProcessed();
                        log.debug("Successfully sent {}: {}", dataType, extractId(item));
                        return true;
                    } catch (Exception e) {
                        log.error("Failed to send {}: {}, error: {}", dataType, extractId(item), e.getMessage(), e);
                        return false;
                    }
                })
                .forEach(_ -> {}); // force execution
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
