package com.aura.clinician.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aura.clinician.api.dto.JustificationItem;
import com.aura.clinician.api.dto.RecommendationItem;
import com.aura.clinician.api.dto.ShapContribution;
import com.aura.clinician.domain.AiPredictionDocument;
import com.aura.clinician.domain.ClinicalReviewDocument;
import com.aura.clinician.domain.PatientCaseDocument;
import com.aura.clinician.repository.AiPredictionRepository;
import com.aura.clinician.repository.ClinicalReviewRepository;
import com.aura.clinician.repository.PatientCaseRepository;
import com.aura.clinician.service.explainability.ExplainabilityProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaseReviewService {
    private final PatientCaseRepository patientCaseRepository;
    private final AiPredictionRepository aiPredictionRepository;
    private final ClinicalReviewRepository clinicalReviewRepository;
    private final ExplainabilityProvider explainabilityProvider;
    private final GuidelineMapper guidelineMapper;
    private final JustificationService justificationService;

    public ClinicalReviewDocument applyReview(String caseId, String finalStatus, String comment) {
        PatientCaseDocument patientCase = patientCaseRepository.findByCaseId(caseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient case not found"));
        AiPredictionDocument prediction = aiPredictionRepository.findByCaseId(caseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI prediction not found"));

        int uctTotal = sumRequired(patientCase.getUct());
        int aectTotal = sumRequired(patientCase.getAect());

        ClinicalReviewDocument.ScoreEntry uct = buildScoreEntry(patientCase.getUct(), uctTotal);
        ClinicalReviewDocument.ScoreEntry aect = buildScoreEntry(patientCase.getAect(), aectTotal);

        List<ShapContribution> shapContributions = explainabilityProvider.getShap(caseId, patientCase);
        List<ClinicalReviewDocument.ShapScore> shapScores = shapContributions.stream()
            .map(feature -> {
                ClinicalReviewDocument.ShapScore score = new ClinicalReviewDocument.ShapScore();
                score.setFeature(feature.getFeature());
                score.setContribution(feature.getContribution());
                return score;
            })
            .toList();

        var gradCam = explainabilityProvider.getGradcam(caseId, patientCase);
        boolean gradCamAvailable = gradCam != null
            && (gradCam.getHeatmapUrl() != null || gradCam.getBaseImageUrl() != null);

        AiPredictionDocument.Risks risks = prediction.getRisks();
        List<ClinicalReviewDocument.RiskEntry> riskEntries = new ArrayList<>();
        riskEntries.add(buildRisk("SIDE_EFFECT", risks != null ? risks.getSideEffect() : null));
        riskEntries.add(buildRisk("HYPERSENSITIVITY", risks != null ? risks.getHypersensitivity() : null));
        riskEntries.add(buildRisk("SECONDARY_DISEASE", risks != null ? risks.getSecondaryDisease() : null));

        List<JustificationItem> justifications = justificationService.buildJustifications(uctTotal, aectTotal, prediction);
        GuidelineContext guidelineContext = new GuidelineContext(
            uctTotal,
            aectTotal,
            prediction.getMultimodelConfidence(),
            prediction.getPredictedStep(),
            riskLevel(risks, "sideEffect"),
            riskScore(risks, "sideEffect"),
            riskLevel(risks, "hypersensitivity"),
            riskScore(risks, "hypersensitivity"),
            riskLevel(risks, "secondaryDisease"),
            riskScore(risks, "secondaryDisease"),
            patientCase.getDailyActivityImpact()
        );
        List<RecommendationItem> recommendations = guidelineMapper.map(
            patientCase.getDiseaseType(),
            guidelineContext,
            justifications
        );
        String recommendationText = recommendations.isEmpty()
            ? null
            : recommendations.stream()
                .map(RecommendationItem::getText)
                .collect(Collectors.joining("; "));

        ClinicalReviewDocument review = new ClinicalReviewDocument();
        review.setCaseId(caseId);
        review.setPatientAge(patientCase.getAgeYears());
        review.setPatientGender(patientCase.getSex());
        review.setHospital(patientCase.getHospital());
        review.setVisitDate(patientCase.getCreatedAt() != null ? patientCase.getCreatedAt() : Instant.now());
        review.setSymptoms(buildSymptoms(patientCase.getSymptoms()));
        review.setUrticariaType(prediction.getSubtype());
        review.setShapeAvailable(patientCase.getShape());
        review.setUct(uct);
        review.setAect(aect);
        review.setGradCamAvailable(gradCamAvailable);
        review.setGradCamHeatMapImage(gradCam != null ? gradCam.getHeatmapUrl() : null);
        review.setImages(gradCam != null && gradCam.getBaseImageUrl() != null
            ? gradCam.getBaseImageUrl()
            : patientCase.getImagePath());
        review.setShapScores(shapScores);
        review.setOverallConfidenceScore(prediction.getUrticariaTypeConfidence());
        review.setRisks(riskEntries);
        review.setPredictedDrug(prediction.getPredictedDrug());
        review.setPredictedStep(prediction.getPredictedStep());
        review.setConfidencePredictedDrugStep(prediction.getMultimodelConfidence());
        review.setRecommendations(recommendationText);
        review.setClinicianFinalStatus(finalStatus);
        review.setComment(comment);

        Instant now = Instant.now();
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        return clinicalReviewRepository.save(review);
    }

    private int sumRequired(PatientCaseDocument.QuestionnaireScore score) {
        if (score == null || score.getQ1() == null || score.getQ2() == null || score.getQ3() == null || score.getQ4() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UCT/AECT scores are incomplete");
        }
        return score.getQ1() + score.getQ2() + score.getQ3() + score.getQ4();
    }

    private ClinicalReviewDocument.ScoreEntry buildScoreEntry(
        PatientCaseDocument.QuestionnaireScore score,
        int total
    ) {
        ClinicalReviewDocument.ScoreEntry entry = new ClinicalReviewDocument.ScoreEntry();
        entry.setTotalScore(total);
        entry.setQ1(score.getQ1());
        entry.setQ2(score.getQ2());
        entry.setQ3(score.getQ3());
        entry.setQ4(score.getQ4());
        return entry;
    }

    private ClinicalReviewDocument.RiskEntry buildRisk(String type, AiPredictionDocument.RiskItem risk) {
        ClinicalReviewDocument.RiskEntry entry = new ClinicalReviewDocument.RiskEntry();
        entry.setRiskType(type);
        if (risk != null) {
            entry.setRiskLevel(risk.getLevel());
            entry.setRiskScore(risk.getScore());
        }
        return entry;
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

    private String buildSymptoms(PatientCaseDocument.Symptoms symptoms) {
        if (symptoms == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        if (symptoms.getItchingScore() != null) {
            parts.add("Itching score: " + symptoms.getItchingScore());
        }
        if (symptoms.getAngioedemaPresent() != null) {
            parts.add("Angioedema present: " + symptoms.getAngioedemaPresent());
        }
        if (symptoms.getAngioedemaDuration() != null) {
            parts.add("Angioedema duration: " + symptoms.getAngioedemaDuration());
        }
        return parts.isEmpty() ? null : String.join(", ", parts);
    }
}
