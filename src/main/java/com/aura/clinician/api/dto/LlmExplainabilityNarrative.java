package com.aura.clinician.api.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LlmExplainabilityNarrative {
    private String summary;
    private String decisionRationale;
    private String tabularEvidence;
    private String imageEvidence;
    private String controlStatus;
    private List<String> recommendations = new ArrayList<>();
    private String safetyNote;
}
