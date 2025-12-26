package com.aura.clinician.provider;

import com.aura.clinician.api.dto.PredictionBlock;

public interface PredictionProvider {
    PredictionBlock getPrediction(String caseId);
}
