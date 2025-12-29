package com.aura.clinician.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ai_predictions")
public class AiPredictionDocument {
    @Id
    private String id;

    private String caseId;
    private String subtype;
    private String predictedStep;
    private String predictedDrug;
    private Double confidence;
    private Double uncertainty;
    private Risks risks;
    private String modelVersion;
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Double getUncertainty() {
        return uncertainty;
    }

    public void setUncertainty(Double uncertainty) {
        this.uncertainty = uncertainty;
    }

    public String getPredictedStep() {
        return predictedStep;
    }

    public void setPredictedStep(String predictedStep) {
        this.predictedStep = predictedStep;
    }

    public String getPredictedDrug() {
        return predictedDrug;
    }

    public void setPredictedDrug(String predictedDrug) {
        this.predictedDrug = predictedDrug;
    }

    public Risks getRisks() {
        return risks;
    }

    public void setRisks(Risks risks) {
        this.risks = risks;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class Risks {
        private RiskItem sideEffect;
        private RiskItem hypersensitivity;
        private RiskItem secondaryDisease;

        public RiskItem getSideEffect() {
            return sideEffect;
        }

        public void setSideEffect(RiskItem sideEffect) {
            this.sideEffect = sideEffect;
        }

        public RiskItem getHypersensitivity() {
            return hypersensitivity;
        }

        public void setHypersensitivity(RiskItem hypersensitivity) {
            this.hypersensitivity = hypersensitivity;
        }

        public RiskItem getSecondaryDisease() {
            return secondaryDisease;
        }

        public void setSecondaryDisease(RiskItem secondaryDisease) {
            this.secondaryDisease = secondaryDisease;
        }
    }

    public static class RiskItem {
        private String level;
        private Double score;

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }
}
