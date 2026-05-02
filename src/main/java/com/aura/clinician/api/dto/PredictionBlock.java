package com.aura.clinician.api.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PredictionBlock {
    private String label;
    private Map<String, Double> probabilities = new LinkedHashMap<>();
    private Double confidence;
    private Double uncertainty;
    private boolean lowConfidence;
    private String interpretation;
}
