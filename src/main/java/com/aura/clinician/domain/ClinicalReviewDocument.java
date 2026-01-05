package com.aura.clinician.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clinical_reviews")
public class ClinicalReviewDocument {
    @Id
    private String id;

    private String caseId;
    private Integer patientAge;
    private String patientGender;
    private String hospital;
    private Instant visitDate;
    private String symptoms;
    private String urticariaType;
    private String shape;
    private Boolean shapeAvailable;
    private ScoreEntry uct;
    private ScoreEntry aect;
    private boolean shapAvailable;
    private boolean gradCamAvailable;
    private String gradCamHeatMapImage;
    private String images;
    private List<ShapScore> shapScores;
    private Double overallConfidenceScore;
    private List<RiskEntry> risks;
    private String predictedDrug;
    private String predictedStep;
    private Double confidencePredictedDrugStep;
    private String recommendations;
    private String clinicianFinalStatus;
    private String finalStatus;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
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

    public Instant getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Instant visitDate) {
        this.visitDate = visitDate;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getUrticariaType() {
        return urticariaType;
    }

    public void setUrticariaType(String urticariaType) {
        this.urticariaType = urticariaType;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public Boolean getShapeAvailable() {
        return shapeAvailable;
    }

    public void setShapeAvailable(Boolean shapeAvailable) {
        this.shapeAvailable = shapeAvailable;
    }

    public ScoreEntry getUct() {
        return uct;
    }

    public void setUct(ScoreEntry uct) {
        this.uct = uct;
    }

    public ScoreEntry getAect() {
        return aect;
    }

    public void setAect(ScoreEntry aect) {
        this.aect = aect;
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

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public List<ShapScore> getShapScores() {
        return shapScores;
    }

    public void setShapScores(List<ShapScore> shapScores) {
        this.shapScores = shapScores;
    }

    public Double getOverallConfidenceScore() {
        return overallConfidenceScore;
    }

    public void setOverallConfidenceScore(Double overallConfidenceScore) {
        this.overallConfidenceScore = overallConfidenceScore;
    }

    public List<RiskEntry> getRisks() {
        return risks;
    }

    public void setRisks(List<RiskEntry> risks) {
        this.risks = risks;
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

    public Double getConfidencePredictedDrugStep() {
        return confidencePredictedDrugStep;
    }

    public void setConfidencePredictedDrugStep(Double confidencePredictedDrugStep) {
        this.confidencePredictedDrugStep = confidencePredictedDrugStep;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getClinicianFinalStatus() {
        return clinicianFinalStatus;
    }

    public void setClinicianFinalStatus(String clinicianFinalStatus) {
        this.clinicianFinalStatus = clinicianFinalStatus;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public static class ScoreEntry {
        private Integer totalScore;
        private Integer q1;
        private Integer q2;
        private Integer q3;
        private Integer q4;

        public Integer getTotalScore() {
            return totalScore;
        }

        public void setTotalScore(Integer totalScore) {
            this.totalScore = totalScore;
        }

        public Integer getQ1() {
            return q1;
        }

        public void setQ1(Integer q1) {
            this.q1 = q1;
        }

        public Integer getQ2() {
            return q2;
        }

        public void setQ2(Integer q2) {
            this.q2 = q2;
        }

        public Integer getQ3() {
            return q3;
        }

        public void setQ3(Integer q3) {
            this.q3 = q3;
        }

        public Integer getQ4() {
            return q4;
        }

        public void setQ4(Integer q4) {
            this.q4 = q4;
        }
    }

    public static class ShapScore {
        private String feature;
        private Double contribution;

        public String getFeature() {
            return feature;
        }

        public void setFeature(String feature) {
            this.feature = feature;
        }

        public Double getContribution() {
            return contribution;
        }

        public void setContribution(Double contribution) {
            this.contribution = contribution;
        }
    }

    public static class RiskEntry {
        private String riskType;
        private String riskLevel;
        private Double riskScore;

        public String getRiskType() {
            return riskType;
        }

        public void setRiskType(String riskType) {
            this.riskType = riskType;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public Double getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(Double riskScore) {
            this.riskScore = riskScore;
        }
    }
}
