package com.pashcevich.data_unifier.dto;

import java.time.Instant;

public record StartsResponse(
        long totalRuns,
        long successfulRuns,
        long failedRuns,
        double successRatePercent ,
        Instant lastSuccessfulRun
) {
}
