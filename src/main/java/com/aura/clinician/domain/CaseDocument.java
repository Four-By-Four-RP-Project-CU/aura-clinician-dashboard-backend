package com.aura.clinician.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cases")
public class CaseDocument {
    @Id
    private String caseId;

    private int patientAge;
    private String patientGender;
    private String hospital;
    private String visitDate;
    private List<String> symptoms;
    private CaseInputDocument.ScoreBlock scores;
    private boolean shapAvailable;
    private boolean gradCamAvailable;
    private String gradCamHeatMapImage;
    private List<String> images;
    private double overallScore;
    private String sideEffectRisk;
    private double hypersensitivitySideEffectRiskScore;
    private String secondaryDiseaseRisk;
    private String predictedDrug;
    private String predictedStep;
    private double confidence;
    private String finalLabel;
    private Instant createdAt;
    private Instant updatedAt;
    private String comment;
    private String finalStatus;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public int getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(int patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public CaseInputDocument.ScoreBlock getScores() {
        return scores;
    }

    public void setScores(CaseInputDocument.ScoreBlock scores) {
        this.scores = scores;
    }

    public boolean isShapAvailable() {
        return shapAvailable;
    }

    public void setShapAvailable(boolean shapAvailable) {
        this.shapAvailable = shapAvailable;
    }

    public boolean isGradCamAvailable() {
        return gradCamAvailable;
    }

    public void setGradCamAvailable(boolean gradCamAvailable) {
        this.gradCamAvailable = gradCamAvailable;
    }

    public String getGradCamHeatMapImage() {
        return gradCamHeatMapImage;
    }

    public void setGradCamHeatMapImage(String gradCamHeatMapImage) {
        this.gradCamHeatMapImage = gradCamHeatMapImage;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public String getSideEffectRisk() {
        return sideEffectRisk;
    }

    public void setSideEffectRisk(String sideEffectRisk) {
        this.sideEffectRisk = sideEffectRisk;
    }

    public double getHypersensitivitySideEffectRiskScore() {
        return hypersensitivitySideEffectRiskScore;
    }

    public void setHypersensitivitySideEffectRiskScore(double hypersensitivitySideEffectRiskScore) {
        this.hypersensitivitySideEffectRiskScore = hypersensitivitySideEffectRiskScore;
    }

    public String getSecondaryDiseaseRisk() {
        return secondaryDiseaseRisk;
    }

    public void setSecondaryDiseaseRisk(String secondaryDiseaseRisk) {
        this.secondaryDiseaseRisk = secondaryDiseaseRisk;
    }

    public String getPredictedDrug() {
        return predictedDrug;
    }

    public void setPredictedDrug(String predictedDrug) {
        this.predictedDrug = predictedDrug;
    }

    public String getPredictedStep() {
        return predictedStep;
    }

    public void setPredictedStep(String predictedStep) {
        this.predictedStep = predictedStep;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getFinalLabel() {
        return finalLabel;
    }

    public void setFinalLabel(String finalLabel) {
        this.finalLabel = finalLabel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }
}
