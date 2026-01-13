package com.pashcevich.data_unifier.config;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;



@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, UnifiedCustomerDto> consumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties()
        );
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
                            record.topic(), record.partition(), record.offset(), exception.getMessage(), exception);
                    },
                    new FixedBackOff(1000L, 3) // Повторные попытки
            );
        factory.setCommonErrorHandler(errorHandler);

        factory.setConcurrency(3);

        return factory;
    }
}
