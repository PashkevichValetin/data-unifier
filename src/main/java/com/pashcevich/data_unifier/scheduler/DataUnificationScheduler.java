package com.pashcevich.data_unifier.scheduler;

import com.pashcevich.data_unifier.service.DataUnificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DataUnificationScheduler {

    private static final String SCHEDULER_PREFIX = "[SCHEDULER] ";
    private static final String MONITOR_PREFIX = "[MONITOR] ";
    private static final int STALE_THRESHOLD_MINUTES = 10;

    private final DataUnificationService dataUnificationService;
    private final SchedulerMetrics metrics;

    @Scheduled(cron = "${app.scheduler.cron:0 */5 * * * *}")
    public void scheduleDataProcessing() {
        Instant startTime = Instant.now();
        log.info("{}Starting scheduled data processing at {}", SCHEDULER_PREFIX, startTime);

        try {
            long beforeCount = dataUnificationService.getProcessedCount();
            dataUnificationService.processAllData();
            long afterCount = dataUnificationService.getProcessedCount();

            metrics.recordSuccess(startTime);
            logProcessingResult(startTime, beforeCount, afterCount);

        } catch (Exception e) {
            metrics.recordFailure();
            log.error("{}Processing failed: {}", SCHEDULER_PREFIX, e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void monitorSchedulerHealth() {
        metrics.checkStaleness(STALE_THRESHOLD_MINUTES);
        log.debug("{}Scheduler stats - {}", MONITOR_PREFIX, metrics.getStats());
    }

    private void logProcessingResult(Instant startTime, long beforeCount, long afterCount) {
        Duration duration = Duration.between(startTime, Instant.now());
        long processed = afterCount - beforeCount;

        log.info("{}Processing completed successfully", SCHEDULER_PREFIX);
        log.info("  ↳ Processed: {}", processed);
        log.info("  ↳ Duration: {} ms", duration.toMillis());
        log.info("  ↳ Total processed: {}", afterCount);
    }

    @Component
    @RequiredArgsConstructor
    public static class SchedulerMetrics {
        private final AtomicInteger successfulRuns = new AtomicInteger(0);
        private final AtomicInteger failedRuns = new AtomicInteger(0);
        private Instant lastSuccessfulRun;

        public void recordSuccess(Instant startTime) {
            successfulRuns.incrementAndGet();
            lastSuccessfulRun = startTime;
        }

        public void recordFailure() {
            failedRuns.incrementAndGet();
        }

        public void checkStaleness(int thresholdMinutes) {
            if (lastSuccessfulRun != null) {
                Duration timeSinceLastRun = Duration.between(lastSuccessfulRun, Instant.now());
                if (timeSinceLastRun.toMinutes() > thresholdMinutes) {
                    log.warn("{}Last successful run was {} minutes ago",
                            MONITOR_PREFIX, timeSinceLastRun.toMinutes());
                }
            }
        }

        public SchedulerStats getStats() {
            return SchedulerStats.builder()
                    .successfulRuns(successfulRuns.get())
                    .failedRuns(failedRuns.get())
                    .lastSuccessfulRun(lastSuccessfulRun) // ✅ Добавлено
                    .totalRuns(successfulRuns.get() + failedRuns.get())
                    .successRate(calculateSuccessRate())
                    .build();
        }

        public Instant getLastSuccessfulRun() {
            return lastSuccessfulRun;
        }

        private double calculateSuccessRate() {
            int total = successfulRuns.get() + failedRuns.get();
            return total > 0 ? (double) successfulRuns.get() / total : 1.0;
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class SchedulerStats {
        private int successfulRuns;
        private int failedRuns;
        private int totalRuns;
        private Instant lastSuccessfulRun;
        private double successRate;
    }
}