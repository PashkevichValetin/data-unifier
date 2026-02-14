package com.pashcevich.data_unifier.adapter.kafka.producer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedDataProducer {

    @Value("${app.kafka.topic.unified-customers:unified-customers}")
    private String customerTopic;

    @Value("${app.kafka.topic.unified-orders:unified-orders}")
    private String ordersTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RetryTemplate retryTemplate;

    public void sendCustomer(UnifiedCustomerDto dto) {
        try {
            kafkaTemplate.send(customerTopic, dto.getUserId().toString(), dto);
            log.debug("Sent customer {} to topic {}", dto.getUserId(), customerTopic);
        } catch (Exception e) {
            log.error("Failed to send customer {} to Kafka", dto.getUserId(), e);
            throw e;
        }
    }

    public void sendOrder(UnifiedOrderDto dto) {
        try {
            kafkaTemplate.send(ordersTopic, dto.getUserId().toString(), dto);
            log.debug("Sent order {} to topic {}", dto.getId(), ordersTopic);
        } catch (Exception e) {
            log.error("Failed to send order {} to Kafka", dto.getId(), e);
            throw e;
        }
    }
}