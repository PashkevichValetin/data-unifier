package com.pashcevich.data_unifier.adapter.kafka.producer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedDataProducer {

    @Value("${app.kafka.topic.unified-customers}")
    private String customerTopic;

    @Value("${app.kafka.topic.unified-orders}")
    private String ordersTopic;

    @Value("${app.kafka.producer.timeout-seconds:30}")
    private int timeoutSeconds;

    private final KafkaTemplate<String, UnifiedCustomerDto> customerKafkaTemplate;
    private final KafkaTemplate<String, UnifiedOrderDto> orderKafkaTemplate;

    public void sendCustomer(UnifiedCustomerDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Customer DTO cannot be null");
        }

        String key = resolveCustomerKey(dto);
        ProducerRecord<String, UnifiedCustomerDto> record = new ProducerRecord<>(customerTopic, key, dto);

        try {
            CompletableFuture<SendResult<String, UnifiedCustomerDto>> future =
                    customerKafkaTemplate.send(customerTopic, key, dto);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Successfully sent customer {} to topic {} partition {} offset {}",
                            key,
                            customerTopic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send customer {} to Kafka. Topic: {}", key, customerTopic, ex);
                }
            });

        } catch (Exception e) {
            log.error("Exception while sending customer {} to Kafka", key, e);
            throw new RuntimeException("Failed to send customer: " + key, e);
        }
    }

    public CompletableFuture<SendResult<String, UnifiedCustomerDto>> sendCustomerAsync(UnifiedCustomerDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Customer DTO cannot be null");
        }

        String key = resolveCustomerKey(dto);

        try {
            CompletableFuture<SendResult<String, UnifiedCustomerDto>> future =
                    customerKafkaTemplate.send(customerTopic, key, dto);

            future.thenAccept(result ->
                    log.debug("Customer {} sent to partition {}", key, result.getRecordMetadata().partition())
            ).exceptionally(ex -> {
                log.error("Failed to send customer {}", key, ex);
                return null;
            });

            return future;

        } catch (Exception e) {
            log.error("Exception while sending customer {} to Kafka", key, e);
            CompletableFuture<SendResult<String, UnifiedCustomerDto>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("Failed to send customer: " + key, e));
            return failed;
        }
    }

    public SendResult<String, UnifiedCustomerDto> sendCustomerSync(UnifiedCustomerDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Customer DTO cannot be null");
        }

        String key = resolveCustomerKey(dto);

        try {
            log.info("Sending customer {} synchronously to topic {}", key, customerTopic);

            CompletableFuture<SendResult<String, UnifiedCustomerDto>> future =
                    customerKafkaTemplate.send(customerTopic, key, dto);

            SendResult<String, UnifiedCustomerDto> result =
                    future.get(timeoutSeconds, TimeUnit.SECONDS);

            log.info("Successfully sent customer {} synchronously. Partition: {}, Offset: {}",
                    key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return result;

        } catch (Exception e) {
            log.error("Failed to send customer {} synchronously after {} seconds", key, timeoutSeconds, e);
            throw new RuntimeException("Failed to send customer synchronously: " + key, e);
        }
    }

    public CompletableFuture<List<SendResult<String, UnifiedCustomerDto>>> sendCustomersBatch(
            List<UnifiedCustomerDto> customers) {

        if (customers == null) {
            throw new IllegalArgumentException("Customers list cannot be null");
        }

        List<CompletableFuture<SendResult<String, UnifiedCustomerDto>>> futures = customers.stream()
                .map(this::sendCustomerAsync)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    public CompletableFuture<Boolean> checkKafkaHealth() {
        try {
            UnifiedCustomerDto testDto = UnifiedCustomerDto.builder()
                    .id(-1L)
                    .userId(-1L)
                    .name("health-check")
                    .email("health@test.com")
                    .type("HEALTH_CHECK")
                    .build();

            return sendCustomerAsync(testDto)
                    .thenApply(result -> true)
                    .exceptionally(ex -> {
                        log.warn("Kafka health check failed", ex);
                        return false;
                    });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    public void sendOrder(UnifiedOrderDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Order DTO cannot be null");
        }

        String key = resolveOrderKey(dto);

        try {
            CompletableFuture<SendResult<String, UnifiedOrderDto>> future =
                    orderKafkaTemplate.send(ordersTopic, key, dto);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Successfully sent order {} to topic {} partition {} offset {}",
                            key,
                            ordersTopic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send order {} to Kafka. Topic: {}", key, ordersTopic, ex);
                }
            });

        } catch (Exception e) {
            log.error("Exception while sending order {} to Kafka", key, e);
            throw new RuntimeException("Failed to send order: " + key, e);
        }
    }

    public CompletableFuture<SendResult<String, UnifiedOrderDto>> sendOrderAsync(UnifiedOrderDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Order DTO cannot be null");
        }

        String key = resolveOrderKey(dto);

        try {
            return orderKafkaTemplate.send(ordersTopic, key, dto);
        } catch (Exception e) {
            log.error("Exception while sending order {} to Kafka", key, e);
            CompletableFuture<SendResult<String, UnifiedOrderDto>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("Failed to send order: " + key, e));
            return failed;
        }
    }

    private String resolveCustomerKey(UnifiedCustomerDto dto) {
        return dto.getUserId() != null ? dto.getUserId().toString() :
                dto.getId() != null ? dto.getId().toString() :
                        "unknown-customer";
    }

    private String resolveOrderKey(UnifiedOrderDto dto) {
        return dto.getOrderId() != null ? dto.getOrderId().toString() :
                dto.getId() != null ? dto.getId().toString() :
                        "unknown-order";
    }
}