package com.aura.clinician.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "prescription_results")
public class PrescriptionResultDocument {
    @Id
    private String id;

    @Field("case_id")
    private String caseId;

    @Field("patient_name")
    private String patientName;

    @Field("module")
    private String module;

    @Field("asset_refs")
    private List<AssetRef> assetRefs;

    @Field("request_payload")
    private RequestPayload requestPayload;

    @Field("result_payload")
    private ResultPayload resultPayload;

    @Field("created_at")
    private Instant createdAt;

    public String getInputAssetFileId() {
        return getAssetFileIdByKind("input_asset");
    }

    public String getAssetFileIdByKind(String kind) {
        if (assetRefs == null || kind == null) return null;
        return assetRefs.stream()
            .filter(a -> kind.equals(a.getKind()))
            .map(AssetRef::getFileId)
            .findFirst()
            .orElse(null);
    }

    @Data
    @NoArgsConstructor
    public static class AssetRef {
        @Field("file_id")
        private String fileId;
        @Field("filename")
        private String filename;
        @Field("content_type")
        private String contentType;
        @Field("kind")
        private String kind;
        @Field("bucket")
        private String bucket;
    }

    @Data
    @NoArgsConstructor
    public static class RequestPayload {
        @Field("lab_overrides")
        private Map<String, Double> labOverrides;
    }

    @Data
    @NoArgsConstructor
    public static class ResultPayload {
        @Field("predicted_drug_group")
        private String predictedDrugGroup;

        @Field("confidence")
        private Double confidence;

        @Field("mapped_guideline_step")
        private String mappedGuidelineStep;

        @Field("guideline_step_detail")
        private GuidelineStepDetail guidelineStepDetail;

        @Field("risk_context_summary")
        private RiskContextSummary riskContextSummary;

        @Field("integrated_clinical_note")
        private String integratedClinicalNote;

        @Field("modality_gate_weights")
        private List<Double> modalityGateWeights;
    }

    @Data
    @NoArgsConstructor
    public static class GuidelineStepDetail {
        @Field("label")
        private String label;
        @Field("drugs")
        private List<String> drugs;
    }

    @Data
    @NoArgsConstructor
    public static class RiskContextSummary {
        @Field("urticaria_type")
        private String urticariaType;

        @Field("severity_band")
        private String severityBand;

        @Field("severity_score")
        private Double severityScore;

        @Field("sideeffect_level")
        private String sideeffectLevel;

        @Field("high_sideeffect_flag")
        private Boolean highSideeffectFlag;

        @Field("thyroid_flag")
        private Boolean thyroidFlag;

        @Field("autoimmune_flag")
        private Boolean autoimmuneFlag;

        @Field("composite_risk_score")
        private Double compositeRiskScore;

        @Field("clinical_interpretation")
        private String clinicalInterpretation;
    }
}
