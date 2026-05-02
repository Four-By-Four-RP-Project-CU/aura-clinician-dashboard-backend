package com.aura.clinician.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "clinical_reviews")
public class ClinicalReviewDocument {
    @Id
    private String id;

    private String caseId;
    private Integer patientAge;
    private String patientGender;
    private String hospital;
    private Instant visitDate;
    private String symptoms;
    private String urticariaType;
    private String shapeAvailable;
    private ScoreEntry uct;
    private ScoreEntry aect;
    private boolean gradCamAvailable;
    private String gradCamHeatMapImage;
    private String images;
    private List<ShapScore> shapScores;
    private Double overallConfidenceScore;
    private List<RiskEntry> risks;
    private String predictedDrug;
    private String predictedStep;
    private Double confidencePredictedDrugStep;
    private String recommendations;
    private String clinicianFinalStatus;
    private String finalStatus;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    public static class ScoreEntry {
        private Integer totalScore;
        private Integer q1;
        private Integer q2;
        private Integer q3;
        private Integer q4;
    }

    @Data
    @NoArgsConstructor
    public static class ShapScore {
        private String feature;
        private Double contribution;
    }

    @Data
    @NoArgsConstructor
    public static class RiskEntry {
        private String riskType;
        private String riskLevel;
        private Double riskScore;
    }
}
