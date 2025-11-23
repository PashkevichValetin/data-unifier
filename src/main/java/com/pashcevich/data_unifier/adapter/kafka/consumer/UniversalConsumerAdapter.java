package com.pashcevich.data_unifier.adapter.kafka.consumer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UniversalConsumerAdapter {

    private final ConcurrentHashMap<String, UnifiedCustomerDto> customerStorage = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${app.kafka.topic.unified-customers:unified-customers}")
    public void consumeUnifiedData(UnifiedCustomerDto customer) {
        try {
            if (customer == null || customer.getCustomerId() == null) {
                log.warn("Received invalid customer data");
                return;
            }

            customerStorage.put(customer.getCustomerId(), customer);

            log.info("Consumed unified data for customer: {} ({} orders)",
                    customer.getCustomerId(),
                    customer.getOrders() != null ? customer.getOrders().size() : 0);

        } catch (Exception e) {
            log.error("Error processing Kafka message for customer: {}",
                    customer != null ? customer.getCustomerId() : "unknown", e);
        }
    }

    public UnifiedCustomerDto getCustomerById(String customerId) {
        return customerStorage.get(customerId);
    }

    public UnifiedCustomerDto getCustomerByUserId(Long userId, String dataSource) {
        String customerId = dataSource.toUpperCase() + "_" + userId;
        return customerStorage.get(customerId);
    }

    public List<UnifiedCustomerDto> getAllCustomers() {
        return new ArrayList<>(customerStorage.values());
    }

    public List<UnifiedCustomerDto> findCustomersByEmail(String email) {
        return customerStorage.values().stream()
                .filter(customer -> email.equalsIgnoreCase(customer.getEmail()))
                .collect(Collectors.toList());
    }

    public List<UnifiedCustomerDto> findCustomersByName(String name) {
        return customerStorage.values().stream()
                .filter(customer -> customer.getName() != null &&
                        customer.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<UnifiedCustomerDto> getCustomersByDataSource(String dataSource) {
        return customerStorage.values().stream()
                .filter(customer -> dataSource.equalsIgnoreCase(customer.getDataSource()))
                .collect(Collectors.toList());
    }

    public boolean customerExists(String customerId) {
        return customerStorage.containsKey(customerId);
    }

    public long getCustomerCount() {
        return customerStorage.size();
    }

    public void clearStorage() {
        customerStorage.clear();
        log.info("Customer storage cleared");
    }
}