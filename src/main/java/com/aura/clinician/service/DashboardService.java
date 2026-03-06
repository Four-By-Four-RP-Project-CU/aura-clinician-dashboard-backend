package com.aura.clinician.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

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
import com.aura.clinician.repository.AiPredictionRepository;
import com.aura.clinician.repository.PatientCaseRepository;
import com.aura.clinician.service.explainability.ExplainabilityProvider;
import com.aura.clinician.service.llm.LlmExplainabilityService;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    private final PatientCaseRepository patientCaseRepository;
    private final AiPredictionRepository aiPredictionRepository;
    private final ExplainabilityProvider explainabilityProvider;
    private final GuidelineMapper guidelineMapper;
    private final JustificationService justificationService;
    private final LlmExplainabilityService llmExplainabilityService;

    public DashboardResponse getDashboard(String caseId, String diseaseType, boolean includeExplainability) {
        logger.info("Building dashboard for caseId={} diseaseType={}", caseId, diseaseType);
        PatientCaseDocument patientCase = patientCaseRepository.findByCaseId(caseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient case not found"));
        AiPredictionDocument prediction = aiPredictionRepository.findByCaseId(caseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI prediction not found"));

        int uctTotal = sumRequired(
            "UCT",
            patientCase.getUct() != null ? patientCase.getUct().getQ1() : null,
            patientCase.getUct() != null ? patientCase.getUct().getQ2() : null,
            patientCase.getUct() != null ? patientCase.getUct().getQ3() : null,
            patientCase.getUct() != null ? patientCase.getUct().getQ4() : null
        );
        int aectTotal = sumRequired(
            "AECT",
            patientCase.getAect() != null ? patientCase.getAect().getQ1() : null,
            patientCase.getAect() != null ? patientCase.getAect().getQ2() : null,
            patientCase.getAect() != null ? patientCase.getAect().getQ3() : null,
            patientCase.getAect() != null ? patientCase.getAect().getQ4() : null
        );

        PatientSummary patientSummary = new PatientSummary();
        patientSummary.setCaseId(caseId);
        patientSummary.setPatientId(patientCase.getPatientId());
        patientSummary.setAge(patientCase.getAgeYears());
        patientSummary.setGender(patientCase.getSex());
        patientSummary.setHospital(patientCase.getHospital());
        patientSummary.setVisitDate(patientCase.getVisitDate());
        patientSummary.setShape(patientCase.getShape());

        PredictionBlock predictionBlock = new PredictionBlock();
        predictionBlock.setLabel(prediction.getSubtype());
        Double confidence = requireDouble("multimodelConfidence", prediction.getMultimodelConfidence());
        predictionBlock.setConfidence(confidence);
        predictionBlock.setUncertainty(prediction.getUncertainty());
        predictionBlock.setLowConfidence(confidence != null && confidence < 0.7);
        predictionBlock.setInterpretation("Subtype prediction derived from AI outputs");

        List<ScoreItem> scores = new ArrayList<>();
        scores.add(buildScore("UCT", "Urticaria Control Test", uctTotal, uctTotal < 12));
        scores.add(buildScore("AECT", "Angioedema Control Test", aectTotal, aectTotal < 10));

        List<RiskItem> risks = new ArrayList<>();
        AiPredictionDocument.Risks predictionRisks = prediction.getRisks();
        risks.add(buildRisk("Side Effect", riskLevel(predictionRisks, "sideEffect"), riskScore(predictionRisks, "sideEffect")));
        risks.add(buildRisk("Hypersensitivity", riskLevel(predictionRisks, "hypersensitivity"), riskScore(predictionRisks, "hypersensitivity")));
        risks.add(buildRisk("Secondary Disease", riskLevel(predictionRisks, "secondaryDisease"), riskScore(predictionRisks, "secondaryDisease")));

        TreatmentPlan treatmentPlan = new TreatmentPlan();
        treatmentPlan.setPredictedStep(prediction.getPredictedStep());
        treatmentPlan.setPredictedDrug(prediction.getPredictedDrug());
        treatmentPlan.setConfidence(confidence);

        String resolvedDiseaseType = diseaseType != null ? diseaseType : patientCase.getDiseaseType();

        ExplanationBlock explanation = new ExplanationBlock();
        if (includeExplainability) {
            List<ShapContribution> shapContributions = explainabilityProvider.getShap(caseId, patientCase);
            explanation.setShapContributions(shapContributions);
            explanation.setShapAvailable(shapContributions != null && !shapContributions.isEmpty());
            GradCamArtifact gradCam = explainabilityProvider.getGradcam(caseId, patientCase);
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
            prediction.getPredictedStep(),
            riskLevel(predictionRisks, "sideEffect"),
            riskScore(predictionRisks, "sideEffect"),
            riskLevel(predictionRisks, "hypersensitivity"),
            riskScore(predictionRisks, "hypersensitivity"),
            riskLevel(predictionRisks, "secondaryDisease"),
            riskScore(predictionRisks, "secondaryDisease"),
            patientCase.getDailyActivityImpact()
        );

        DashboardResponse response = new DashboardResponse();
        response.setCaseId(caseId);
        response.setDiseaseType(resolvedDiseaseType);
        response.setPatientSummary(patientSummary);
        response.setDiseaseControlInfo(buildDiseaseControlInfo(uctTotal));
        response.setPrediction(predictionBlock);
        response.setScores(scores);
        response.setRisks(risks);
        response.setTreatmentPlan(treatmentPlan);
        response.setExplanation(explanation);
        response.setJustifications(justifications);
        List<RecommendationItem> mappedRecommendations =
            guidelineMapper.map(resolvedDiseaseType, guidelineContext, justifications);
        response.setRecommendations(mappedRecommendations);

        if (includeExplainability) {
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
        if (uctTotal < 12 || aectTotal < 10) {
            warnings.add("CONTROL_SCORE_ALERT");
        }
        response.setWarningFlags(warnings);

        logger.info("Dashboard built for caseId={} with {} recommendations", caseId, response.getRecommendations().size());
        return response;
    }

    private int sumRequired(String label, Integer... values) {
        int total = 0;
        for (Integer value : values) {
            if (value == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " scores are incomplete");
            }
            total += value;
        }
        return total;
    }

    private Double requireDouble(String label, Double value) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " is missing");
        }
        return value;
    }

    private ScoreItem buildScore(String code, String label, int value, boolean warning) {
        ScoreItem score = new ScoreItem();
        score.setCode(code);
        score.setLabel(label);
        score.setValue(value);
        score.setInterpretation(warning ? "Below control target" : "Within control target");
        score.setWarning(warning);
        return score;
    }

    private RiskItem buildRisk(String type, String level, Double score) {
        RiskItem item = new RiskItem();
        item.setType(type);
        item.setLevel(level);
        item.setScore(score);
        return item;
    }

    private DiseaseControlInfo buildDiseaseControlInfo(int uctTotal) {
        DiseaseControlInfo info = new DiseaseControlInfo();
        if (uctTotal >= 12) {
            info.setStatus("CONTROLLED");
            info.setTooltip("Symptoms are well controlled. The patient reports minimal or no impact from urticaria in daily life.");
            return info;
        }
        if (uctTotal >= 8) {
            info.setStatus("PARTIALLY_CONTROLLED");
            info.setTooltip("Symptoms are present but not fully controlled. The patient experiences intermittent symptoms or moderate impact on daily activities.");
            return info;
        }
        info.setStatus("UNCONTROLLED");
        info.setTooltip("Symptoms are poorly controlled. The patient reports frequent or severe symptoms with significant impact on daily life.");
        return info;
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
}
