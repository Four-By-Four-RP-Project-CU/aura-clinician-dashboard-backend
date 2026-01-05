package com.aura.clinician.api.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class PredictionBlock {
    private String label;
    private Map<String, Double> probabilities = new LinkedHashMap<>();
    private Double confidence;
    private Double uncertainty;
    private boolean lowConfidence;
    private String interpretation;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Double> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(Map<String, Double> probabilities) {
        this.probabilities = probabilities;
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

    public boolean isLowConfidence() {
        return lowConfidence;
    }

    public void setLowConfidence(boolean lowConfidence) {
        this.lowConfidence = lowConfidence;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }
}
