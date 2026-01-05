package com.aura.clinician.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrainCaseDto {
    private Integer patientAge;
    private String gender;
    private ScoreDto uct;
    private ScoreDto aect;
    private Double confidencePredictedDrugStep;

    private String predictedDrug;
    private String predictedStep;

    // Python expects adminDecision, you can map clinicianFinalDecision into this field
    private String adminDecision;

    private String correctedDrug;
    private String correctedStep;
}
