package com.aura.clinician.api.dto.ResponseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardInsightsResponseDto {
    private DashboardRetrainingCoverageResponseDto retrainingCoverage;
    private DashboardDatasetReadinessResponseDto datasetReadiness;
    private DashboardRecentActivityResponseDto recentActivity;
    private DashboardLabelCoverageResponseDto labelCoverage;
}

