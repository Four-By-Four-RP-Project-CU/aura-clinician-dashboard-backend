package com.aura.clinician.provider;

import com.aura.clinician.api.dto.ExplanationBlock;

public interface ExplainabilityProvider {
    ExplanationBlock getExplanation(String caseId);
}
