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
public class DashboardRetrainingCoverageResponseDto {
    private long retrainedCases;
    private long targetCases;
    private double progressPercent;
    private String currentModelVersion;
    private int confidenceTargetPercent;
    private Instant lastUpdatedAt;
}

