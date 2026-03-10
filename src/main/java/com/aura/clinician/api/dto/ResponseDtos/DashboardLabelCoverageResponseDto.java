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
public class DashboardLabelCoverageResponseDto {
    private long reviewedCases;
    private long targetReviewedCases;
    private long acceptedCount;
    private long correctedCount;
    private long rejectedCount;
    private boolean readyForRetraining;
    private Instant lastUpdatedAt;
}

