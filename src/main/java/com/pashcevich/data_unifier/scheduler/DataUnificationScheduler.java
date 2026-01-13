package com.pashcevich.data_unifier.scheduler;

import com.pashcevich.data_unifier.service.DataUnificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataUnificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataUnificationScheduler.class);

    @Autowired
    private DataUnificationService dataUnificationService;

    // Запуск каждые 30 минут
    @Scheduled(fixedRate = 1800000)
    public void scheduleDataProcessing() {
        try {
            logger.info("Scheduled data processing started");
            dataUnificationService.processAllData();
            logger.info("Scheduled data processing completed successfully");
        } catch (Exception e) {
            logger.error("Error in scheduled data processing", e);
        }
    }
}
