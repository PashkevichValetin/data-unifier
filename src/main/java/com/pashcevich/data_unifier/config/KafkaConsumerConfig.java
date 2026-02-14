package com.pashcevich.data_unifier.config;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, UnifiedCustomerDto> consumerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.TYPE_MAPPINGS,
                "unifiedCustomerDto:com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UnifiedCustomerDto> kafkaListenerContainerFactory(
            ConsumerFactory<String, UnifiedCustomerDto> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, UnifiedCustomerDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.error("Failed to process message from topic {} partition {} offset {}: {}",
                            record.topic(), record.partition(), record.offset(),
                            exception.getMessage(), exception);
                },
                new FixedBackOff(1000L, 3)
        );
        factory.setCommonErrorHandler(errorHandler);
        factory.setConcurrency(3);

        return factory;
    }
}