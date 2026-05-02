package com.aura.clinician.domain;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

    @Data
    @NoArgsConstructor
    public static class Labs {
        @Field("CRP")
        private Double crp;
        @Field("FT4")
        private Double ft4;
        @Field("IgE")
        private Double igE;
        @Field("VitD")
        private Double vitD;
    }

    @Data
    @NoArgsConstructor
    public static class QuestionnaireScore {
        @Field("Q1")
        private Integer q1;
        @Field("Q2")
        private Integer q2;
        @Field("Q3")
        private Integer q3;
        @Field("Q4")
        private Integer q4;
    }

    @Data
    @NoArgsConstructor
    public static class Symptoms {
        private String itchingScore;
        private Boolean angioedemaPresent;
        private String angioedemaDuration;
    }
}
