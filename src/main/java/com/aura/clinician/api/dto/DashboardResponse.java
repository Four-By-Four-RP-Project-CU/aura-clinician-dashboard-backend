package com.aura.clinician.api.dto;

import java.util.ArrayList;
import java.util.List;

public class DashboardResponse {
    private String caseId;
    private String diseaseType;
    private String interpretationSummary;
    private List<String> warningFlags = new ArrayList<>();
    private PredictionBlock prediction;
    private List<ScoreItem> scores = new ArrayList<>();
    private ExplanationBlock explanation;
    private List<RecommendationItem> recommendations = new ArrayList<>();
    private AuditMetadata audit;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getDiseaseType() {
        return diseaseType;
    }

    public void setDiseaseType(String diseaseType) {
        this.diseaseType = diseaseType;
    }

    public String getInterpretationSummary() {
        return interpretationSummary;
    }

    public void setInterpretationSummary(String interpretationSummary) {
        this.interpretationSummary = interpretationSummary;
    }

    public List<String> getWarningFlags() {
        return warningFlags;
    }

    public void setWarningFlags(List<String> warningFlags) {
        this.warningFlags = warningFlags;
    }

    public PredictionBlock getPrediction() {
        return prediction;
    }

    public void setPrediction(PredictionBlock prediction) {
        this.prediction = prediction;
    }

    public List<ScoreItem> getScores() {
        return scores;
    }

    public void setScores(List<ScoreItem> scores) {
        this.scores = scores;
    }

    public ExplanationBlock getExplanation() {
        return explanation;
    }

    public void setExplanation(ExplanationBlock explanation) {
        this.explanation = explanation;
    }

    public List<RecommendationItem> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationItem> recommendations) {
        this.recommendations = recommendations;
    }

    public AuditMetadata getAudit() {
        return audit;
    }

    public void setAudit(AuditMetadata audit) {
        this.audit = audit;
    }
}
