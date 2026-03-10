package com.aura.clinician.api.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DashboardResponse {
    private String caseId;
    private String diseaseType;
    private String interpretationSummary;
    private List<String> warningFlags = new ArrayList<>();
    private PatientSummary patientSummary;
    private DiseaseControlInfo diseaseControlInfo;
    private PredictionBlock prediction;
    private List<ScoreItem> scores = new ArrayList<>();
    private List<RiskItem> risks = new ArrayList<>();
    private TreatmentPlan treatmentPlan;
    private ExplanationBlock explanation;
    private LlmExplainabilityNarrative llmExplainability;
    private List<JustificationItem> justifications = new ArrayList<>();
    private List<RecommendationItem> recommendations = new ArrayList<>();
}
