package com.aura.clinician.api.dto.ResponseDtos;

import com.aura.clinician.Enums.TrainingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCurrentDeployedModelResponseDto {
    private String modelVersion;
    private TrainingStatus modelStatus;
    private Instant trainedAt;
    private Integer trainedCases;
    private Double accuracyPercent;
    private Double macroF1Percent;
    private Double stepAccuracyPercent;
    private List<String> featureColumns;
}

