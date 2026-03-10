package com.aura.clinician.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TreatmentPlan {
    private String predictedStep;
    private String predictedDrug;
    private Double confidence;
}
