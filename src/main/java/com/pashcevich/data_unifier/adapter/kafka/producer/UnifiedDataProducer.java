package com.pashcevich.data_unifier.adapter.kafka.producer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedDataProducer {
    private final KafkaTemplate<String, UnifiedCustomerDto> kafkaTemplate;

    public void sendUnifiedCustomer(UnifiedCustomerDto customer) {
        try {
            kafkaTemplate.send("unified-customer", customer);
            log.info("Send unified data to Kafka for user: {}", customer.getUserId());
        } catch (Exception e) {
            log.error("Failed to send unified data for user: {} ", customer.getUserId(), e);
        }
    }
}
