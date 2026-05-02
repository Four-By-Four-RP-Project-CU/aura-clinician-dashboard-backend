package com.aura.clinician.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "risk_results")
public class RiskResultDocument {
    @Id
    private String id;

    @Field("case_id")
    private String caseId;

    @Field("patient_id")
    private String patientId;

    @Field("sex")
    private String sex;

    @Field("request_payload")
    private RequestPayload requestPayload;

    @Field("result_payload")
    private ResultPayload resultPayload;

    @Field("created_at")
    private Instant createdAt;

    /** Returns sex from the top-level field first, falling back to request_payload.categorical.Sex */
    public String resolvedSex() {
        if (sex != null && !sex.isBlank()) return sex;
        if (requestPayload != null && requestPayload.getCategorical() != null) {
            return requestPayload.getCategorical().getSex();
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    public static class RequestPayload {
        @Field("categorical")
        private Categorical categorical;
    }

    @Data
    @NoArgsConstructor
    public static class Categorical {
        @Field("Sex")
        private String sex;
    }

    @Data
    @NoArgsConstructor
    public static class ResultPayload {
        @Field("urticaria_type")
        private AiPredictionDocument.UrticariaType urticariaType;

        @Field("secondary_disease_risk")
        private AiPredictionDocument.SecondaryDiseaseRisk secondaryDiseaseRisk;

        @Field("sideeffect_risk")
        private AiPredictionDocument.SideEffectRisk sideeffectRisk;

        @Field("severity")
        private AiPredictionDocument.Severity severity;

        @Field("composite_risk_score")
        private Double compositeRiskScore;

        @Field("clinical_interpretation")
        private String clinicalInterpretation;

        @Field("modality_gates")
        private Map<String, Double> modalityGates = new LinkedHashMap<>();
    }
}
