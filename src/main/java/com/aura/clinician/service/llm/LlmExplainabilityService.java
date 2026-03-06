package com.aura.clinician.service.llm;

import com.aura.clinician.api.dto.LlmEvidencePayload;
import com.aura.clinician.api.dto.LlmExplainabilityNarrative;

public interface LlmExplainabilityService {
    LlmExplainabilityNarrative generateNarrative(LlmEvidencePayload payload);
}
