package com.aura.clinician.provider.cu;

import org.springframework.stereotype.Component;

import com.aura.clinician.api.dto.PredictionBlock;
import com.aura.clinician.domain.CaseInputDocument;
import com.aura.clinician.provider.PredictionProvider;
import com.aura.clinician.repository.CaseInputRepository;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CuPredictionProvider implements PredictionProvider {
    private final CaseInputRepository caseInputRepository;

    public CuPredictionProvider(CaseInputRepository caseInputRepository) {
        this.caseInputRepository = caseInputRepository;
    }

    @Override
    public PredictionBlock getPrediction(String caseId) {
        PredictionBlock block = new PredictionBlock();
        CaseInputDocument input = caseInputRepository.findByCaseId(caseId).orElse(null);
        String label = input != null && input.getFinalLabel() != null ? input.getFinalLabel() : "CSU";
        double confidence = input != null ? input.getConfidence() : 0.72;
        double uncertainty = Math.max(0, 1 - confidence);

        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("CSU", label.equals("CSU") ? confidence : 1 - confidence);
        probabilities.put("CIndU", label.equals("CIndU") ? confidence : 1 - confidence);
        block.setProbabilities(probabilities);
        block.setLabel(label);
        block.setConfidence(confidence);
        block.setUncertainty(uncertainty);
        block.setInterpretation("Likely urticaria subtype based on current evidence");
        return block;
    }
}
