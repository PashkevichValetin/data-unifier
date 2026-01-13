package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.mysql.MySQLOrderAdapter;
import com.pashcevich.data_unifier.adapter.postgres.PostgresUserAdapter;
import com.pashcevich.data_unifier.adapter.kafka.producer.UnifiedDataProducer;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataUnificationServiceImpl implements DataUnificationService {

    private static final Logger logger = LoggerFactory.getLogger(DataUnificationServiceImpl.class);

    @Autowired
    private PostgresUserAdapter postgresUserAdapter;

    @Autowired
    private MySQLOrderAdapter mySQLOrderAdapter;

    @Autowired
    private UnifiedDataProducer unifiedDataProducer;

    @Override
    public void processUserData() {
        try {
            logger.info("Processing user data from PostgreSQL");
            // Здесь будет логика обработки пользователей
            logger.info("User data processing completed successfully");
        } catch (Exception e) {
            logger.error("Error processing user data", e);
            throw new DataUnificationException("Failed to process user data", e);
        }
    }

    @Override
    public void processOrderData() {
        try {
            logger.info("Processing order data from MySQL");
            // Здесь будет логика обработки заказов
            logger.info("Order data processing completed successfully");
        } catch (Exception e) {
            logger.error("Error processing order data", e);
            throw new DataUnificationException("Failed to process order data", e);
        }
    }

    @Override
    public void processAllData() {
        try {
            logger.info("Starting complete data processing");
            processUserData();
            processOrderData();
            logger.info("Complete data processing completed successfully");
        } catch (Exception e) {
            logger.error("Error in complete data processing", e);
            throw new DataUnificationException("Failed to complete data processing", e);
        }
    }
}












