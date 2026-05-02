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
@Document(collection = "ai_predictions")
public class AiPredictionDocument {
    @Id
    private String id;

    private String caseId;
    private String subtype;
    private String predictedStep;
    private String predictedDrug;
    private Double multimodelConfidence;
    private Double urticariaTypeConfidence;
    private Double uncertainty;
    private Risks risks;
    private String modelVersion;
    private Instant createdAt;

    @Field("urticaria_type")
    private UrticariaType urticariaType;

    @Field("secondary_disease_risk")
    private SecondaryDiseaseRisk secondaryDiseaseRisk;

    @Field("sideeffect_risk")
    private SideEffectRisk sideeffectRisk;

    @Field("severity")
    private Severity severity;

    @Field("composite_risk_score")
    private Double compositeRiskScore;

    @Field("clinical_interpretation")
    private String clinicalInterpretation;

    @Field("modality_gates")
    private Map<String, Double> modalityGates;

    public String getSubtype() {
        if (subtype != null && !subtype.isBlank()) {
            return subtype;
        }
        if (urticariaType != null && urticariaType.getPredicted() != null && !urticariaType.getPredicted().isBlank()) {
            return urticariaType.getPredicted();
        }
        return null;
    }

    public String getPredictedStep() {
        if (predictedStep != null && !predictedStep.isBlank()) {
            return predictedStep;
        }
        if (severity != null && severity.getBand() != null) {
            return "SEVERE".equalsIgnoreCase(severity.getBand()) ? "Step-Up" : "Maintain";
        }
        return null;
    }

    public String getPredictedDrug() {
        if (predictedDrug != null && !predictedDrug.isBlank()) {
            return predictedDrug;
        }
        if (severity != null && severity.getBand() != null) {
            return "SEVERE".equalsIgnoreCase(severity.getBand())
                ? "Second-line / biologic therapy consideration"
                : "Optimize current therapy";
        }
        return null;
    }

    public Double getMultimodelConfidence() {
        if (multimodelConfidence != null) {
            return multimodelConfidence;
        }
        if (urticariaType != null && urticariaType.getConfidencePct() != null) {
            return urticariaType.getConfidencePct() / 100.0;
        }
        return null;
    }

    public Double getUrticariaTypeConfidence() {
        if (urticariaTypeConfidence != null) {
            return urticariaTypeConfidence;
        }
        return getMultimodelConfidence();
    }

    public Double getUncertainty() {
        if (uncertainty != null) {
            return uncertainty;
        }
        Double confidence = getMultimodelConfidence();
        return confidence != null ? Math.max(0.0, 1.0 - confidence) : null;
    }

    public Risks getRisks() {
        if (risks != null) {
            return risks;
        }
        Risks resolved = new Risks();

        if (sideeffectRisk != null) {
            RiskItem side = new RiskItem();
            side.setLevel(sideeffectRisk.getLevel());
            if (sideeffectRisk.getDistribution() != null) {
                Double high = sideeffectRisk.getDistribution().get("HIGH");
                Double moderate = sideeffectRisk.getDistribution().get("MODERATE");
                Double low = sideeffectRisk.getDistribution().get("LOW");
                if (high != null) {
                    side.setScore(high / 100.0);
                } else if (moderate != null) {
                    side.setScore(moderate / 100.0);
                } else if (low != null) {
                    side.setScore(low / 100.0);
                }
            }
            resolved.setSideEffect(side);
        }

        if (secondaryDiseaseRisk != null) {
            RiskItem secondary = new RiskItem();
            Double secondaryScore = secondaryDiseaseRisk.getAutoimmuneRiskPct();
            if (secondaryScore == null) {
                secondaryScore = secondaryDiseaseRisk.getThyroidRiskPct();
            }
            if (secondaryScore != null) {
                secondary.setScore(secondaryScore / 100.0);
                secondary.setLevel(riskLevelFromPercent(secondaryScore));
            }
            resolved.setSecondaryDisease(secondary);

            RiskItem hypersensitivity = new RiskItem();
            Double thyroidScore = secondaryDiseaseRisk.getThyroidRiskPct();
            if (thyroidScore != null) {
                hypersensitivity.setScore(thyroidScore / 100.0);
                hypersensitivity.setLevel(riskLevelFromPercent(thyroidScore));
            }
            resolved.setHypersensitivity(hypersensitivity);
        }

        return resolved;
    }

    private String riskLevelFromPercent(Double value) {
        if (value == null) {
            return null;
        }
        if (value >= 70.0) {
            return "HIGH";
        }
        if (value >= 35.0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    @Data
    @NoArgsConstructor
    public static class Risks {
        private RiskItem sideEffect;
        private RiskItem hypersensitivity;
        private RiskItem secondaryDisease;
    }

    @Data
    @NoArgsConstructor
    public static class RiskItem {
        private String level;
        private Double score;
    }

    @Data
    @NoArgsConstructor
    public static class UrticariaType {
        private String predicted;
        @Field("confidence_pct")
        private Double confidencePct;
        private Map<String, Double> distribution = new LinkedHashMap<>();
    }

    @Data
    @NoArgsConstructor
    public static class SecondaryDiseaseRisk {
        @Field("thyroid_risk_pct")
        private Double thyroidRiskPct;
        @Field("autoimmune_risk_pct")
        private Double autoimmuneRiskPct;
        @Field("thyroid_flag")
        private Boolean thyroidFlag;
        @Field("autoimmune_flag")
        private Boolean autoimmuneFlag;
    }

    @Data
    @NoArgsConstructor
    public static class SideEffectRisk {
        private String level;
        private Map<String, Double> distribution = new LinkedHashMap<>();
        @Field("high_risk_flag")
        private Boolean highRiskFlag;
    }

    @Data
    @NoArgsConstructor
    public static class Severity {
        @Field("predicted_score")
        private Double predictedScore;
        @Field("uncertainty_95ci")
        private java.util.List<Double> uncertainty95ci;
        private String band;
        private String description;
    }
}
