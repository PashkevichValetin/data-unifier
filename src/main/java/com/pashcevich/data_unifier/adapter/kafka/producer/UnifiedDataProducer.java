package com.pashcevich.data_unifier.adapter.kafka.producer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedDataProducer {

    private final KafkaTemplate<String, UnifiedCustomerDto> kafkaTemplate;

    @Value("${app.kafka.topic.unified-customers:unified-customers}")
    private String topicName;

    public void sendUnifiedCustomer(UnifiedCustomerDto customer) {
        try {
            String key = customer.getCustomerId();

            CompletableFuture<SendResult<String, UnifiedCustomerDto>> future =
                    kafkaTemplate.send(topicName, key, customer);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Successfully sent customer {} to partition {}",
                            customer.getCustomerId(),
                            result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send customer: {}", customer.getCustomerId(), ex);
                }
            });

            log.info("Sent unified data to Kafka for customer: {}", customer.getCustomerId());

        } catch (Exception e) {
            log.error("Unexpected error sending customer: {}", customer.getCustomerId(), e);
        }
    }

    public void sendUnifiedCustomers(List<UnifiedCustomerDto> customers) {
        if (customers == null || customers.isEmpty()) {
            log.warn("Attempted to send empty customers list");
            return;
        }

        log.info("Sending batch of {} customers to Kafka", customers.size());

        customers.forEach(this::sendUnifiedCustomer);

        log.info("Completed batch send of {} customers", customers.size());
    }

    public boolean sendUnifiedCustomerSync(UnifiedCustomerDto customer) {
        try {
            SendResult<String, UnifiedCustomerDto> result = kafkaTemplate.send(
                    topicName, customer.getCustomerId(), customer
            ).get();

            log.debug("Sync send successful for customer: {}, partition: {}",
                    customer.getCustomerId(), result.getRecordMetadata().partition());
            return true;

        } catch (Exception e) {
            log.error("Sync send failed for customer: {}", customer.getCustomerId(), e);
            return false;
        }
    }
}