package com.pashcevich.data_unifier.adapter.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UniversalConsumerAdapter {
    private final ConcurrentHashMap<Long, UnifiedCustomerDto> customerData = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic.unified-customers}")
    public void consumeUnifiedData(String unifiedCustomerData) {
        try {
            UnifiedCustomerDto customer = objectMapper.readValue(unifiedCustomerData, UnifiedCustomerDto.class);
            customerData.put(customer.getUserId(), customer);
            log.info("Consumed unified data for user: {}", customer.getUserId());
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", unifiedCustomerData, e);
        }
    }

    public UnifiedCustomerDto getCustomerById(Long userId) {
        return customerData.get(userId);
    }

    public List<UnifiedCustomerDto> getAllCustomers() {
        return List.copyOf(customerData.values());
    }

    public boolean customerExists(Long userId) {
        return customerData.containsKey(userId);
    }
}