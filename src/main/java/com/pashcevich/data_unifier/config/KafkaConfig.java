package com.pashcevich.data_unifier.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.unified-customers:unified-customers}")
    private String unifiedCustomersTopic;

    @Bean
    public NewTopic unifiedCustomersTopic() {
        return TopicBuilder.name(unifiedCustomersTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
