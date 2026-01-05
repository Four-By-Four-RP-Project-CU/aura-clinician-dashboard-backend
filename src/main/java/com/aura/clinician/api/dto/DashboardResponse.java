package com.aura.clinician.api.dto;

import java.util.ArrayList;
import java.util.List;

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
    private List<JustificationItem> justifications = new ArrayList<>();
    private List<RecommendationItem> recommendations = new ArrayList<>();

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

    public PatientSummary getPatientSummary() {
        return patientSummary;
    }

    public void setPatientSummary(PatientSummary patientSummary) {
        this.patientSummary = patientSummary;
    }

    public DiseaseControlInfo getDiseaseControlInfo() {
        return diseaseControlInfo;
    }

    public void setDiseaseControlInfo(DiseaseControlInfo diseaseControlInfo) {
        this.diseaseControlInfo = diseaseControlInfo;
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

    public List<RiskItem> getRisks() {
        return risks;
    }

    public void setRisks(List<RiskItem> risks) {
        this.risks = risks;
    }

    public TreatmentPlan getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(TreatmentPlan treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public ExplanationBlock getExplanation() {
        return explanation;
    }

    public void setExplanation(ExplanationBlock explanation) {
        this.explanation = explanation;
    }

    public List<JustificationItem> getJustifications() {
        return justifications;
    }

    public void setJustifications(List<JustificationItem> justifications) {
        this.justifications = justifications;
    }

    public List<RecommendationItem> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationItem> recommendations) {
        this.recommendations = recommendations;
    }

}
