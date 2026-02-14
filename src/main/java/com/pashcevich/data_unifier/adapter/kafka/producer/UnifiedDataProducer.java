package com.pashcevich.data_unifier.adapter.kafka.producer;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UnifiedDataProducer {

    @Value("${kafka.topic.unified}")
    private String topic;

    private final KafkaTemplate<String, UnifiedCustomerDto> kafkaTemplate;
    private final RetryTemplate retryTemplate;

    public void send(UnifiedCustomerDto dto) {
        kafkaTemplate.send(topic, dto.getId().toString(), dto);
    }
}