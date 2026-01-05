package com.aura.clinician.domain;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "patient_cases")
public class PatientCaseDocument {
    @Id
    private String id;

    @Field("caseId")
    private String caseId;

    @Field("diseaseType")
    private String diseaseType;

    @Field("patient_id")
    private String patientId;

    @Field("visit_id")
    private String visitId;

    @Field("visitDate")
    private String visitDate;

    @Field("age_years")
    private Integer ageYears;
    private String sex;
    private String hospital;

    @Field("labs")
    private Labs labs;

    @Field("UCT")
    private QuestionnaireScore uct;

    @Field("AECT")
    private QuestionnaireScore aect;

    @Field("symptoms")
    private Symptoms symptoms;

    @Field("overallImpact")
    private String overallImpact;

    @Field("dailyActivityImpact")
    private String dailyActivityImpact;

    @Field("image_path")
    private String imagePath;

    @Field("shape")
    private String shape;

    @Field("createdAt")
    private Instant createdAt;

    private Map<String, String> questionnaire;

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

    public String getDiseaseType() {
        return diseaseType;
    }

    public void setDiseaseType(String diseaseType) {
        this.diseaseType = diseaseType;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public Integer getAgeYears() {
        return ageYears;
    }

    public void setAgeYears(Integer ageYears) {
        this.ageYears = ageYears;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public Labs getLabs() {
        return labs;
    }

    public void setLabs(Labs labs) {
        this.labs = labs;
    }

    public QuestionnaireScore getUct() {
        return uct;
    }

    public void setUct(QuestionnaireScore uct) {
        this.uct = uct;
    }

    public QuestionnaireScore getAect() {
        return aect;
    }

    public void setAect(QuestionnaireScore aect) {
        this.aect = aect;
    }

    public Symptoms getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(Symptoms symptoms) {
        this.symptoms = symptoms;
    }

    public String getOverallImpact() {
        return overallImpact;
    }

    public void setOverallImpact(String overallImpact) {
        this.overallImpact = overallImpact;
    }

    public String getDailyActivityImpact() {
        return dailyActivityImpact;
    }

    public void setDailyActivityImpact(String dailyActivityImpact) {
        this.dailyActivityImpact = dailyActivityImpact;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, String> getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(Map<String, String> questionnaire) {
        this.questionnaire = questionnaire;
    }

    public static class Labs {
        @Field("CRP")
        private Double crp;
        @Field("FT4")
        private Double ft4;
        @Field("IgE")
        private Double igE;
        @Field("VitD")
        private Double vitD;

        public Double getCrp() {
            return crp;
        }

        public void setCrp(Double crp) {
            this.crp = crp;
        }

        public Double getFt4() {
            return ft4;
        }

        public void setFt4(Double ft4) {
            this.ft4 = ft4;
        }

        public Double getIgE() {
            return igE;
        }

        public void setIgE(Double igE) {
            this.igE = igE;
        }

        public Double getVitD() {
            return vitD;
        }

        public void setVitD(Double vitD) {
            this.vitD = vitD;
        }
    }

    public static class QuestionnaireScore {
        @Field("Q1")
        private Integer q1;
        @Field("Q2")
        private Integer q2;
        @Field("Q3")
        private Integer q3;
        @Field("Q4")
        private Integer q4;

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

    public static class Symptoms {
        private String itchingScore;
        private Boolean angioedemaPresent;
        private String angioedemaDuration;

        public String getItchingScore() {
            return itchingScore;
        }

        public void setItchingScore(String itchingScore) {
            this.itchingScore = itchingScore;
        }

        public Boolean getAngioedemaPresent() {
            return angioedemaPresent;
        }

        public void setAngioedemaPresent(Boolean angioedemaPresent) {
            this.angioedemaPresent = angioedemaPresent;
        }

        public String getAngioedemaDuration() {
            return angioedemaDuration;
        }

        public void setAngioedemaDuration(String angioedemaDuration) {
            this.angioedemaDuration = angioedemaDuration;
        }
    }
}
