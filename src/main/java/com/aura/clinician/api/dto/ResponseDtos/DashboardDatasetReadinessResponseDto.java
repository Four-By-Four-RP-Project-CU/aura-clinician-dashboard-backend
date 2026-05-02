package com.aura.clinician.api.dto.ResponseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDatasetReadinessResponseDto {
    private long reviewedCases;
    private long totalCases;
    private double coverageRatePercent;
    private long correctionsLogged;
    private long acceptedPredictions;
    private int targetRangeStart;
    private int targetRangeEnd;
    private boolean minimumMet;
    private Instant lastUpdatedAt;
}
