package com.aura.clinician.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.DashboardResponse;
import com.aura.clinician.api.dto.DiseaseControlInfo;
import com.aura.clinician.api.dto.ExplanationBlock;
import com.aura.clinician.api.dto.GradCamArtifact;
import com.aura.clinician.api.dto.JustificationItem;
import com.aura.clinician.api.dto.LlmEvidencePayload;
import com.aura.clinician.api.dto.LlmExplainabilityNarrative;
import com.aura.clinician.api.dto.PatientSummary;
import com.aura.clinician.api.dto.PredictionBlock;
import com.aura.clinician.api.dto.RecommendationItem;
import com.aura.clinician.api.dto.RiskItem;
import com.aura.clinician.api.dto.ScoreItem;
import com.aura.clinician.api.dto.TreatmentPlan;
import com.aura.clinician.api.dto.ShapContribution;
import com.aura.clinician.domain.AiPredictionDocument;
import com.aura.clinician.domain.PatientCaseDocument;
import com.aura.clinician.service.explainability.ExplainabilityProvider;
import com.aura.clinician.service.llm.LlmExplainabilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    private final CaseContextService caseContextService;
    private final ExplainabilityProvider explainabilityProvider;
    private final GuidelineMapper guidelineMapper;
    private final JustificationService justificationService;
    private final LlmExplainabilityService llmExplainabilityService;

    public DashboardResponse getDashboard(
        String caseId,
        String diseaseType,
        boolean includeExplainability,
        boolean includeLlm
    ) {
        logger.info("Building dashboard for caseId={} diseaseType={}", caseId, diseaseType);
        CaseContextService.CaseContext caseContext = caseContextService.getCaseContext(caseId);
        PatientCaseDocument patientCase = caseContext.getPatientCase();
        AiPredictionDocument prediction = caseContext.getPrediction();
        Integer uctTotal = caseContext.getUctTotal();
        Integer aectTotal = caseContext.getAectTotal();

        PatientSummary patientSummary = new PatientSummary();
        patientSummary.setCaseId(caseId);
        patientSummary.setPatientId(patientCase != null ? patientCase.getPatientId() : null);
        patientSummary.setAge(patientCase != null ? patientCase.getAgeYears() : null);
        patientSummary.setGender(resolveGender(patientCase));
        patientSummary.setHospital(patientCase != null ? patientCase.getHospital() : null);
        patientSummary.setVisitDate(caseContext.getVisitDateText());
        patientSummary.setShape(patientCase != null ? patientCase.getShape() : null);

        PredictionBlock predictionBlock = new PredictionBlock();
        predictionBlock.setLabel(prediction != null ? prediction.getSubtype() : null);
        Double confidence = prediction != null ? prediction.getMultimodelConfidence() : null;
        predictionBlock.setConfidence(confidence);
        predictionBlock.setUncertainty(prediction != null ? prediction.getUncertainty() : null);
        predictionBlock.setLowConfidence(confidence != null && confidence < 0.7);
        predictionBlock.setInterpretation("Subtype prediction derived from AI outputs");

        List<ScoreItem> scores = new ArrayList<>();
        scores.add(buildScore("UCT", "Urticaria Control Test", uctTotal, uctTotal != null && uctTotal < 12));
        scores.add(buildScore("AECT", "Angioedema Control Test", aectTotal, aectTotal != null && aectTotal < 10));

        List<RiskItem> risks = new ArrayList<>();
        AiPredictionDocument.Risks predictionRisks = prediction != null ? prediction.getRisks() : null;
        risks.add(buildRisk("Side Effect", riskLevel(predictionRisks, "sideEffect"), riskScore(predictionRisks, "sideEffect")));
        risks.add(buildRisk("Hypersensitivity", riskLevel(predictionRisks, "hypersensitivity"), riskScore(predictionRisks, "hypersensitivity")));
        risks.add(buildRisk("Secondary Disease", riskLevel(predictionRisks, "secondaryDisease"), riskScore(predictionRisks, "secondaryDisease")));

        TreatmentPlan treatmentPlan = new TreatmentPlan();
        treatmentPlan.setPredictedStep(prediction != null ? prediction.getPredictedStep() : null);
        treatmentPlan.setPredictedDrug(prediction != null ? prediction.getPredictedDrug() : null);
        treatmentPlan.setConfidence(confidence);

        String resolvedDiseaseType = diseaseType != null
            ? diseaseType
            : (patientCase != null ? patientCase.getDiseaseType() : "CU");

        ExplanationBlock explanation = new ExplanationBlock();
        if (includeExplainability) {
            CompletableFuture<List<ShapContribution>> shapFuture = CompletableFuture.supplyAsync(() ->
                explainabilityProvider.getShap(caseId, patientCase)
            );
            CompletableFuture<GradCamArtifact> gradCamFuture = CompletableFuture.supplyAsync(() ->
                explainabilityProvider.getGradcam(caseId, patientCase)
            );

            List<ShapContribution> shapContributions;
            GradCamArtifact gradCam;
            try {
                shapContributions = shapFuture.join();
            } catch (CompletionException ex) {
                logger.warn("SHAP fetch failed for caseId={}: {}", caseId, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                shapContributions = new ArrayList<>();
            }
            try {
                gradCam = gradCamFuture.join();
            } catch (CompletionException ex) {
                logger.warn("Grad-CAM fetch failed for caseId={}: {}", caseId, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                gradCam = null;
            }

            explanation.setShapContributions(shapContributions);
            explanation.setShapAvailable(shapContributions != null && !shapContributions.isEmpty());
            explanation.setGradCam(gradCam);
            explanation.setGradCamAvailable(gradCam != null
                && (gradCam.getHeatmapUrl() != null || gradCam.getBaseImageUrl() != null));
        } else {
            explanation.setShapContributions(new ArrayList<>());
            explanation.setShapAvailable(false);
            explanation.setGradCam(null);
            explanation.setGradCamAvailable(false);
        }

        List<JustificationItem> justifications = justificationService.buildJustifications(uctTotal, aectTotal, prediction);

        GuidelineContext guidelineContext = new GuidelineContext(
            uctTotal,
            aectTotal,
            confidence,
            prediction != null ? prediction.getPredictedStep() : null,
            riskLevel(predictionRisks, "sideEffect"),
            riskScore(predictionRisks, "sideEffect"),
            riskLevel(predictionRisks, "hypersensitivity"),
            riskScore(predictionRisks, "hypersensitivity"),
            riskLevel(predictionRisks, "secondaryDisease"),
            riskScore(predictionRisks, "secondaryDisease"),
            patientCase != null ? patientCase.getDailyActivityImpact() : null
        );

        DashboardResponse response = new DashboardResponse();
        response.setCaseId(caseId);
        response.setDiseaseType(resolvedDiseaseType);
        response.setPatientSummary(patientSummary);
        AiPredictionDocument.Severity severity = prediction != null ? prediction.getSeverity() : null;
        response.setDiseaseControlInfo(buildDiseaseControlInfo(uctTotal, severity));
        response.setPrediction(predictionBlock);
        response.setScores(scores);
        response.setRisks(risks);
        response.setTreatmentPlan(treatmentPlan);
        response.setExplanation(explanation);
        response.setJustifications(justifications);
        List<RecommendationItem> mappedRecommendations =
            guidelineMapper.map(resolvedDiseaseType, guidelineContext, justifications);
        response.setRecommendations(mappedRecommendations);

        if (includeExplainability && includeLlm) {
            LlmEvidencePayload evidencePayload = buildLlmEvidencePayload(
                predictionBlock,
                scores,
                explanation,
                mappedRecommendations,
                treatmentPlan,
                collectMissingData(patientCase, prediction, explanation)
            );
            LlmExplainabilityNarrative narrative = llmExplainabilityService.generateNarrative(evidencePayload);
            response.setLlmExplainability(narrative);
        }

        List<String> warnings = new ArrayList<>();
        if (predictionBlock.isLowConfidence()) {
            warnings.add("LOW_CONFIDENCE");
        }
        if ((uctTotal != null && uctTotal < 12) || (aectTotal != null && aectTotal < 10)) {
            warnings.add("CONTROL_SCORE_ALERT");
        }
        response.setWarningFlags(warnings);

        logger.info("Dashboard built for caseId={} with {} recommendations", caseId, response.getRecommendations().size());
        return response;
    }

    private ScoreItem buildScore(String code, String label, Integer value, boolean warning) {
        ScoreItem score = new ScoreItem();
        score.setCode(code);
        score.setLabel(label);
        score.setValue(value != null ? value.doubleValue() : null);
        score.setInterpretation(
            value == null
                ? "Not available"
                : (warning ? "Below control target" : "Within control target")
        );
        score.setWarning(value != null && warning);
        return score;
    }

    private RiskItem buildRisk(String type, String level, Double score) {
        RiskItem item = new RiskItem();
        item.setType(type);
        item.setLevel(level);
        item.setScore(score);
        return item;
    }

    private DiseaseControlInfo buildDiseaseControlInfo(Integer uctTotal, AiPredictionDocument.Severity severity) {
        DiseaseControlInfo info = new DiseaseControlInfo();

        // UCT is the gold-standard: 4-question patient-reported questionnaire (EAACI guideline).
        // UCT 16 = completely controlled, 12-15 = well-controlled, <12 = uncontrolled.
        if (uctTotal != null) {
            if (uctTotal >= 12) {
                info.setStatus("CONTROLLED");
                info.setTooltip("UCT " + uctTotal + "/16 — Symptoms are well controlled.");
            } else if (uctTotal >= 8) {
                info.setStatus("PARTIALLY_CONTROLLED");
                info.setTooltip("UCT " + uctTotal + "/16 — Symptoms are partially controlled.");
            } else {
                info.setStatus("UNCONTROLLED");
                info.setTooltip("UCT " + uctTotal + "/16 — Symptoms are poorly controlled.");
            }
            return info;
        }

        // UCT not collected — fall back to AI-predicted severity band (EAACI: SEVERE/MODERATE/MILD).
        if (severity != null && severity.getBand() != null) {
            String band = severity.getBand().toUpperCase();
            switch (band) {
                case "MILD" -> {
                    info.setStatus("CONTROLLED");
                    info.setTooltip("Based on AI severity (MILD, score " + fmt(severity.getPredictedScore()) + "/10). UCT questionnaire not collected.");
                }
                case "MODERATE" -> {
                    info.setStatus("PARTIALLY_CONTROLLED");
                    info.setTooltip("Based on AI severity (MODERATE, score " + fmt(severity.getPredictedScore()) + "/10). UCT questionnaire not collected.");
                }
                case "SEVERE" -> {
                    info.setStatus("UNCONTROLLED");
                    info.setTooltip("Based on AI severity (SEVERE, score " + fmt(severity.getPredictedScore()) + "/10). UCT questionnaire not collected.");
                }
                default -> {
                    info.setStatus("UNKNOWN");
                    info.setTooltip("Disease control could not be determined.");
                }
            }
            return info;
        }

        info.setStatus("UNKNOWN");
        info.setTooltip("UCT questionnaire not collected and severity data unavailable.");
        return info;
    }

    private String fmt(Double v) {
        return v != null ? String.format("%.1f", v) : "N/A";
    }

    private String riskLevel(AiPredictionDocument.Risks risks, String type) {
        AiPredictionDocument.RiskItem item = riskItem(risks, type);
        return item != null ? item.getLevel() : null;
    }

    private Double riskScore(AiPredictionDocument.Risks risks, String type) {
        AiPredictionDocument.RiskItem item = riskItem(risks, type);
        return item != null ? item.getScore() : null;
    }

    private AiPredictionDocument.RiskItem riskItem(AiPredictionDocument.Risks risks, String type) {
        if (risks == null) {
            return null;
        }
        return switch (type) {
            case "sideEffect" -> risks.getSideEffect();
            case "hypersensitivity" -> risks.getHypersensitivity();
            case "secondaryDisease" -> risks.getSecondaryDisease();
            default -> null;
        };
    }

    private LlmEvidencePayload buildLlmEvidencePayload(
        PredictionBlock prediction,
        List<ScoreItem> scores,
        ExplanationBlock explanation,
        List<RecommendationItem> recommendations,
        TreatmentPlan treatmentPlan,
        List<String> missingData
    ) {
        LlmEvidencePayload payload = new LlmEvidencePayload();
        payload.setSubtypePredictionLabel(prediction != null ? prediction.getLabel() : null);
        payload.setConfidence(prediction != null ? prediction.getConfidence() : null);

        ScoreItem uct = scoreByCode(scores, "UCT");
        ScoreItem aect = scoreByCode(scores, "AECT");
        payload.setUctScore(uct != null ? uct.getValue() : null);
        payload.setUctStatus(uct != null ? uct.getInterpretation() : null);
        payload.setAectScore(aect != null ? aect.getValue() : null);
        payload.setAectStatus(aect != null ? aect.getInterpretation() : null);

        if (explanation != null && explanation.getShapContributions() != null) {
            payload.setTopShapFeatures(
                explanation.getShapContributions().stream().limit(5).toList()
            );
        }
        if (explanation != null && explanation.getGradCam() != null) {
            payload.setGradCamHeatmapUrl(explanation.getGradCam().getHeatmapUrl());
            payload.setGradCamBaseImageUrl(explanation.getGradCam().getBaseImageUrl());
        }
        payload.setGradCamSummary(
            (payload.getGradCamHeatmapUrl() != null || payload.getGradCamBaseImageUrl() != null)
                ? "Grad-CAM artifacts available."
                : "Grad-CAM artifacts unavailable."
        );

        if (recommendations != null) {
            payload.setRecommendations(
                recommendations.stream()
                    .map(RecommendationItem::getText)
                    .filter(text -> text != null && !text.isBlank())
                    .toList()
            );
        }

        List<String> treatmentPath = new ArrayList<>();
        if (treatmentPlan != null && treatmentPlan.getPredictedStep() != null) {
            treatmentPath.add("Predicted step: " + treatmentPlan.getPredictedStep());
        }
        if (treatmentPlan != null && treatmentPlan.getPredictedDrug() != null) {
            treatmentPath.add("Predicted drug: " + treatmentPlan.getPredictedDrug());
        }
        payload.setTreatmentPathwayList(treatmentPath);
        payload.setMissingData(missingData);
        return payload;
    }

    private ScoreItem scoreByCode(List<ScoreItem> scores, String code) {
        if (scores == null) {
            return null;
        }
        return scores.stream()
            .filter(item -> code.equalsIgnoreCase(item.getCode()))
            .findFirst()
            .orElse(null);
    }

    private List<String> collectMissingData(
        PatientCaseDocument patientCase,
        AiPredictionDocument prediction,
        ExplanationBlock explanation
    ) {
        List<String> missing = new ArrayList<>();
        if (patientCase == null || patientCase.getLabs() == null) {
            missing.add("labs");
        }
        if (prediction == null || prediction.getMultimodelConfidence() == null) {
            missing.add("multimodelConfidence");
        }
        if (explanation == null || explanation.getShapContributions() == null || explanation.getShapContributions().isEmpty()) {
            missing.add("shapContributions");
        }
        if (explanation == null || explanation.getGradCam() == null || explanation.getGradCam().getHeatmapUrl() == null) {
            missing.add("gradCamHeatmap");
        }
        return missing;
    }

    private String resolveGender(PatientCaseDocument patientCase) {
        if (patientCase == null || patientCase.getSex() == null || patientCase.getSex().isBlank()) {
            return "Unknown";
        }
        return patientCase.getSex();
    }
}
