package com.pashcevich.data_unifier.controller;

import com.pashcevich.data_unifier.dto.StartsResponse;
import com.pashcevich.data_unifier.scheduler.DataUnificationScheduler;
import com.pashcevich.data_unifier.service.DataUnificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@ConditionalOnBean(DataUnificationScheduler.class)
@ConditionalOnExpression("${scheduler.enabled:true} == true")
public class SchedulerController {

    private final DataUnificationService dataUnificationService;

    private final AtomicLong successfulRuns = new AtomicLong(0);
    private final AtomicLong failedRuns = new AtomicLong(0);
    private final AtomicReference<Instant> lastSuccessfulRun = new AtomicReference<>(null);
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerProcessing() {
        if (!isProcessing.compareAndSet(false, true)) {
            log.warn("Processing already in progress - request rejected");
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Processing already in progress",
                    "retry_after_seconds", 30
            ));
        }

        log.info("Processing triggered manually");
        try {
            dataUnificationService.processAllData();
            lastSuccessfulRun.set(Instant.now());
            successfulRuns.incrementAndGet();

            return ResponseEntity.ok(Map.of(
                    "message", "Processing triggered successfully",
                    "timestamp", lastSuccessfulRun.get()
            ));
        } catch (Exception e) {
            failedRuns.incrementAndGet();
            log.error("Failed to trigger processing", e);

            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to trigger processing",
                    "details", e.getMessage(),
                    "timestamp", Instant.now()
            ));
        } finally {
            isProcessing.set(false);
            log.debug("Processing lock released");
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<StartsResponse> getStats() {
        long success = successfulRuns.get();
        long failed = failedRuns.get();
        long totalRuns = success + failed;
        double successRate = totalRuns == 0 ? 0 : (double) success / totalRuns * 100;

        return ResponseEntity.ok(new StartsResponse(
                totalRuns,
                success,
                failed,
                Math.round(successRate * 100.0) / 100.0,
                lastSuccessfulRun.get()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Instant lastRun = lastSuccessfulRun.get();

        if (lastRun == null) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DEGRADED",
                    "message", "No successful runs yet",
                    "last_run", "never"
            ));
        }

        long minutesSinceLastRun = Instant.now().getEpochSecond() - lastRun.getEpochSecond() / 60;

        if (minutesSinceLastRun > 30) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DEGRADED",
                    "message", "Last successful run was more than 30 minutes ago",
                    "last_run", lastRun.toString(),
                    "minutes_since_last_run", minutesSinceLastRun
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Service is healthy",
                "last_run", lastRun.toString(),
                "minutes_since_last_run", minutesSinceLastRun
        ));
    }
}