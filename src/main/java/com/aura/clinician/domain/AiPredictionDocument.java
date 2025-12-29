package com.aura.clinician.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "ai_predictions")
public class AiPredictionDocument {
    @Id
    private String id;

    private String caseId;
    private String subtype;
    private String predictedStep;
    private String predictedDrug;
    private Double multimodelConfidence;
    private Double urticariaTypeConfidence;
    private Double uncertainty;
    private Risks risks;
    private String modelVersion;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    public static class Risks {
        private RiskItem sideEffect;
        private RiskItem hypersensitivity;
        private RiskItem secondaryDisease;
    }

    @Data
    @NoArgsConstructor
    public static class RiskItem {
        private String level;
        private Double score;
    }
}
