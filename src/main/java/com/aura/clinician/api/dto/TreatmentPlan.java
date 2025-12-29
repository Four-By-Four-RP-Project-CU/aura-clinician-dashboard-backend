package com.aura.clinician.api.dto;

public class TreatmentPlan {
    private String predictedStep;
    private String predictedDrug;
    private Double confidence;

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

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
