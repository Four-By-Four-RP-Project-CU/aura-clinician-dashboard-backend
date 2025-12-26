package com.aura.clinician.provider.cu;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.aura.clinician.api.dto.PredictionBlock;
import com.aura.clinician.provider.PredictionProvider;

@Component
public class CuPredictionProvider implements PredictionProvider {
    @Override
    public PredictionBlock getPrediction(String caseId) {
        PredictionBlock block = new PredictionBlock();
        block.setLabel("CSU");

        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("CSU", 0.72);
        probabilities.put("CIndU", 0.28);
        block.setProbabilities(probabilities);

        block.setConfidence(0.72);
        block.setUncertainty(0.28);
        block.setInterpretation("Likely chronic spontaneous urticaria based on current evidence");
        return block;
    }
}
