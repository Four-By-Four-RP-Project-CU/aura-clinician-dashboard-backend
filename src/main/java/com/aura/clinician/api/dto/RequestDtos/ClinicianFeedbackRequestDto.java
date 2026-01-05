package com.aura.clinician.api.dto.RequestDtos;

import com.aura.clinician.Enums.ClinicianFinalDecision;
import com.aura.clinician.models.ScoreBlockModel;
import lombok.Data;

@Data
public class ClinicianFeedbackRequestDto {
    private Integer patientAge;
    private String patientGender;
    private ScoreBlockModel uct;
    private ScoreBlockModel aect;
    private String predictedDrug;
    private String predictedStep;
    private Double confidencePredictedDrugStep;
    private ClinicianFinalDecision clinicianFinalDecision;
    private String correctedDrug;
    private String correctedStep;
    private String reviewedBy;
    private String comment;

}
