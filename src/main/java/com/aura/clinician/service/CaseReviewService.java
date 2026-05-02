package com.aura.clinician.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.JustificationItem;
import com.aura.clinician.api.dto.RecommendationItem;
import com.aura.clinician.api.dto.ShapContribution;
import com.aura.clinician.domain.AiPredictionDocument;
import com.aura.clinician.domain.ClinicalReviewDocument;
import com.aura.clinician.domain.PatientCaseDocument;
import com.aura.clinician.domain.PrescriptionResultDocument;
import com.aura.clinician.repository.ClinicalReviewRepository;
import com.aura.clinician.repository.PrescriptionResultRepository;
import com.aura.clinician.service.explainability.ExplainabilityProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaseReviewService {
    private final CaseContextService caseContextService;
    private final ClinicalReviewRepository clinicalReviewRepository;
    private final PrescriptionResultRepository prescriptionResultRepository;
    private final ImageBase64Service imageBase64Service;
    private final ExplainabilityProvider explainabilityProvider;
    private final GuidelineMapper guidelineMapper;
    private final JustificationService justificationService;

    public ClinicalReviewDocument applyReview(String caseId, String finalStatus, String comment) {
        CaseContextService.CaseContext caseContext = caseContextService.getCaseContext(caseId);
        PatientCaseDocument patientCase = caseContext.getPatientCase();
        AiPredictionDocument prediction = caseContext.getPrediction();

        Integer uctTotal = caseContext.getUctTotal();
        Integer aectTotal = caseContext.getAectTotal();

        ClinicalReviewDocument.ScoreEntry uct = buildScoreEntry(uctTotal);
        ClinicalReviewDocument.ScoreEntry aect = buildScoreEntry(aectTotal);

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

        // Resolve base64 images from GridFS asset_refs
        PrescriptionResultDocument prescription =
            prescriptionResultRepository.findByCaseId(caseId).orElse(null);
        String gradCamBase64 = resolveBase64(prescription, "gradcam",
            gradCam != null ? gradCam.getHeatmapUrl() : null);
        String inputImageBase64 = resolveBase64(prescription, "input_asset",
            gradCam != null ? gradCam.getBaseImageUrl()
                : (patientCase != null ? patientCase.getImagePath() : null));

        AiPredictionDocument.Risks risks = prediction != null ? prediction.getRisks() : null;
        List<ClinicalReviewDocument.RiskEntry> riskEntries = new ArrayList<>();
        riskEntries.add(buildRisk("SIDE_EFFECT", risks != null ? risks.getSideEffect() : null));
        riskEntries.add(buildRisk("HYPERSENSITIVITY", risks != null ? risks.getHypersensitivity() : null));
        riskEntries.add(buildRisk("SECONDARY_DISEASE", risks != null ? risks.getSecondaryDisease() : null));

        List<JustificationItem> justifications = justificationService.buildJustifications(uctTotal, aectTotal, prediction);
        GuidelineContext guidelineContext = new GuidelineContext(
            uctTotal,
            aectTotal,
            prediction != null ? prediction.getMultimodelConfidence() : null,
            prediction != null ? prediction.getPredictedStep() : null,
            riskLevel(risks, "sideEffect"),
            riskScore(risks, "sideEffect"),
            riskLevel(risks, "hypersensitivity"),
            riskScore(risks, "hypersensitivity"),
            riskLevel(risks, "secondaryDisease"),
            riskScore(risks, "secondaryDisease"),
            patientCase != null ? patientCase.getDailyActivityImpact() : null
        );
        List<RecommendationItem> recommendations = guidelineMapper.map(
            patientCase != null && patientCase.getDiseaseType() != null ? patientCase.getDiseaseType() : "CU",
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
        review.setPatientAge(patientCase != null ? patientCase.getAgeYears() : null);
        review.setPatientGender(patientCase != null ? patientCase.getSex() : null);
        review.setHospital(patientCase != null ? patientCase.getHospital() : null);
        review.setVisitDate(caseContext.getVisitInstant() != null ? caseContext.getVisitInstant() : Instant.now());
        review.setSymptoms(patientCase != null ? buildSymptoms(patientCase.getSymptoms()) : null);
        review.setUrticariaType(prediction != null ? prediction.getSubtype() : null);
        review.setShapeAvailable(patientCase != null ? patientCase.getShape() : null);
        review.setUct(uct);
        review.setAect(aect);
        review.setGradCamAvailable(gradCamAvailable);
        review.setGradCamHeatMapImage(gradCamBase64);
        review.setImages(inputImageBase64);
        review.setShapScores(shapScores);
        review.setOverallConfidenceScore(prediction != null ? prediction.getUrticariaTypeConfidence() : null);
        review.setRisks(riskEntries);
        review.setPredictedDrug(prediction != null ? prediction.getPredictedDrug() : null);
        review.setPredictedStep(prediction != null ? prediction.getPredictedStep() : null);
        review.setConfidencePredictedDrugStep(prediction != null ? prediction.getMultimodelConfidence() : null);
        review.setRecommendations(recommendationText);
        review.setClinicianFinalStatus(finalStatus);
        review.setComment(comment);

        Instant now = Instant.now();
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        return clinicalReviewRepository.save(review);
    }

    private ClinicalReviewDocument.ScoreEntry buildScoreEntry(Integer total) {
        ClinicalReviewDocument.ScoreEntry entry = new ClinicalReviewDocument.ScoreEntry();
        entry.setTotalScore(total);
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

    /**
     * Resolves a base64 data URI for an image.
     * Priority: GridFS fileId from asset_refs → GridFS fileId parsed from fallback URL.
     */
    private String resolveBase64(PrescriptionResultDocument prescription, String kind, String fallbackUrl) {
        // 1. Try GridFS directly via asset_refs
        if (prescription != null) {
            String fileId = prescription.getAssetFileIdByKind(kind);
            if (fileId != null) {
                String b64 = imageBase64Service.toBase64DataUri(fileId);
                if (b64 != null) return b64;
            }
        }
        // 2. Parse fileId from a URL like .../gridfs/{fileId}
        if (fallbackUrl != null) {
            int idx = fallbackUrl.lastIndexOf("/gridfs/");
            if (idx >= 0) {
                String fileId = fallbackUrl.substring(idx + "/gridfs/".length());
                String b64 = imageBase64Service.toBase64DataUri(fileId);
                if (b64 != null) return b64;
            }
        }
        return null;
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
