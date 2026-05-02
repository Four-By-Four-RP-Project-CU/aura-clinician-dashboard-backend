package com.aura.clinician.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "cases")
public class CaseDocument {
    @Id
    private String caseId;

    private int patientAge;
    private String patientGender;
    private String hospital;
    private String visitDate;
    private List<String> symptoms;
    private CaseInputDocument.ScoreBlock scores;
    private boolean shapAvailable;
    private boolean gradCamAvailable;
    private String gradCamHeatMapImage;
    private List<String> images;
    private double overallScore;
    private String sideEffectRisk;
    private double hypersensitivitySideEffectRiskScore;
    private String secondaryDiseaseRisk;
    private String predictedDrug;
    private String predictedStep;
    private double confidence;
    private String finalLabel;
    private Instant createdAt;
    private Instant updatedAt;
    private String comment;
    private String finalStatus;
}
