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

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@ConditionalOnBean(DataUnificationScheduler.class)
@ConditionalOnExpression("${scheduler.enabled:true} == true")
public class SchedulerController {

    private final DataUnificationService dataUnificationService;
    private final DataUnificationScheduler.SchedulerMetrics schedulerMetrics;

    // ✅ Добавлено: блокировка на одновременный запуск
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
            schedulerMetrics.recordSuccess(Instant.now());
            return ResponseEntity.ok(Map.of(
                    "message", "Processing triggered successfully",
                    "timestamp", Instant.now()
            ));
        } catch (Exception e) {
            schedulerMetrics.recordFailure();
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
        var stats = schedulerMetrics.getStats();
        return ResponseEntity.ok(new StartsResponse(
                stats.getTotalRuns(),
                stats.getSuccessfulRuns(),
                stats.getFailedRuns(),
                stats.getSuccessRate(),
                stats.getLastSuccessfulRun()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        var lastRun = schedulerMetrics.getLastSuccessfulRun();
        if (lastRun == null) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DEGRADED",
                    "message", "No successful runs yet",
                    "last_run", "never"
            ));
        }

        long minutesSinceLastRun = Duration.between(lastRun, Instant.now()).toMinutes();

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
