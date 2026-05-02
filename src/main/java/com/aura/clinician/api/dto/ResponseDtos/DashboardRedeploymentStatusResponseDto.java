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
public class DashboardRedeploymentStatusResponseDto {
    private String currentModelVersion;
    private boolean isLive;
    private long eligibleReviewedCases;
    private int confidenceTargetPercent;
    private boolean updatedModelDeployed;
    private boolean activeLearningEnabled;
    private String redeploymentStatusMessage;
    private Instant lastUpdatedAt;
}

