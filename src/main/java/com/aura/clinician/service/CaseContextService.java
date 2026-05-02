package com.aura.clinician.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aura.clinician.domain.AiPredictionDocument;
import com.aura.clinician.domain.PatientCaseDocument;
import com.aura.clinician.domain.PrescriptionResultDocument;
import com.aura.clinician.domain.PrescriptionResultDocument.RequestPayload;
import com.aura.clinician.domain.PrescriptionResultDocument.ResultPayload;
import com.aura.clinician.domain.PrescriptionResultDocument.RiskContextSummary;
import com.aura.clinician.domain.RiskResultDocument;
import com.aura.clinician.repository.PrescriptionResultRepository;
import com.aura.clinician.repository.RiskResultRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaseContextService {
    private final PrescriptionResultRepository prescriptionResultRepository;
    private final RiskResultRepository riskResultRepository;

    @Value("${clinician.base-url:http://localhost:8081}")
    private String clinicianBaseUrl;

    public CaseContext getCaseContext(String caseId) {
        PrescriptionResultDocument prescription = prescriptionResultRepository.findByCaseId(caseId).orElse(null);
        RiskResultDocument risk = riskResultRepository.findByCaseId(caseId).orElse(null);

        if (prescription == null && risk == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found");
        }
        return buildContext(caseId, prescription, risk);
    }

    public List<CaseContext> getAllCaseContexts() {
        List<PrescriptionResultDocument> prescriptions = prescriptionResultRepository.findAll();
        List<RiskResultDocument> risks = riskResultRepository.findAll();

        Map<String, PrescriptionResultDocument> prescriptionByCaseId = new LinkedHashMap<>();
        for (PrescriptionResultDocument prescription : prescriptions) {
            if (prescription.getCaseId() != null && !prescription.getCaseId().isBlank()) {
                prescriptionByCaseId.put(prescription.getCaseId(), prescription);
            }
        }

        Map<String, RiskResultDocument> riskByCaseId = new LinkedHashMap<>();
        for (RiskResultDocument risk : risks) {
            if (risk.getCaseId() != null && !risk.getCaseId().isBlank()) {
                riskByCaseId.put(risk.getCaseId(), risk);
            }
        }

        LinkedHashSet<String> caseIds = new LinkedHashSet<>();
        caseIds.addAll(prescriptionByCaseId.keySet());
        caseIds.addAll(riskByCaseId.keySet());

        List<CaseContext> contexts = new ArrayList<>();
        for (String caseId : caseIds) {
            contexts.add(buildContext(caseId, prescriptionByCaseId.get(caseId), riskByCaseId.get(caseId)));
        }
        return contexts;
    }

    private CaseContext buildContext(
        String caseId,
        PrescriptionResultDocument prescription,
        RiskResultDocument risk
    ) {
        PatientCaseDocument patientCase = buildPatientCase(caseId, prescription, risk);
        AiPredictionDocument prediction = buildPrediction(caseId, prescription, risk);
        Integer uctTotal = extractScoreTotal(prescription, "UCT_total", "UCT", "uct_total", "uct");
        if (uctTotal == null) {
            uctTotal = deriveUctFromSeverity(risk);
        }
        Integer aectTotal = extractScoreTotal(prescription, "AECT_total", "AECT", "aect_total", "aect");
        if (aectTotal == null) {
            aectTotal = deriveAectFromRisk(risk);
        }
        Instant createdAt = firstNonNull(
            risk != null ? risk.getCreatedAt() : null,
            prescription != null ? prescription.getCreatedAt() : null,
            Instant.now()
        );
        String visitDate = createdAt != null ? createdAt.toString() : null;

        return new CaseContext(caseId, patientCase, prediction, uctTotal, aectTotal, createdAt, visitDate);
    }

    private PatientCaseDocument buildPatientCase(
        String caseId,
        PrescriptionResultDocument prescription,
        RiskResultDocument risk
    ) {
        PatientCaseDocument patientCase = new PatientCaseDocument();
        patientCase.setCaseId(caseId);
        patientCase.setDiseaseType("CU");
        patientCase.setPatientId(risk != null ? risk.getPatientId() : null);
        patientCase.setSex(normalizeBlank(risk != null ? risk.resolvedSex() : null));

        Instant createdAt = firstNonNull(
            risk != null ? risk.getCreatedAt() : null,
            prescription != null ? prescription.getCreatedAt() : null
        );
        patientCase.setCreatedAt(createdAt);
        patientCase.setVisitDate(createdAt != null ? createdAt.toString() : null);

        Map<String, Double> labOverrides = labOverridesOf(prescription);
        if (!labOverrides.isEmpty()) {
            PatientCaseDocument.Labs labs = new PatientCaseDocument.Labs();
            labs.setCrp(firstDouble(labOverrides, "CRP", "crp"));
            labs.setFt4(firstDouble(labOverrides, "FT4", "ft4"));
            labs.setIgE(firstDouble(labOverrides, "IgE", "ige", "IGE"));
            labs.setVitD(firstDouble(labOverrides, "VitD", "vitd", "VITD", "VitaminD", "vitamin_d"));
            patientCase.setLabs(labs);

            Double age = firstDouble(labOverrides, "Age", "AGE", "age");
            patientCase.setAgeYears(age != null ? age.intValue() : null);
        }

        if (prescription != null) {
            String fileId = prescription.getInputAssetFileId();
            if (fileId != null && !fileId.isBlank()) {
                patientCase.setImagePath(clinicianBaseUrl + "/api/v1/images/gridfs/" + fileId);
            }
        }

        return patientCase;
    }

    private AiPredictionDocument buildPrediction(
        String caseId,
        PrescriptionResultDocument prescription,
        RiskResultDocument risk
    ) {
        AiPredictionDocument prediction = new AiPredictionDocument();
        prediction.setCaseId(caseId);
        prediction.setCreatedAt(firstNonNull(
            risk != null ? risk.getCreatedAt() : null,
            prescription != null ? prescription.getCreatedAt() : null
        ));

        if (risk != null && risk.getResultPayload() != null) {
            RiskResultDocument.ResultPayload payload = risk.getResultPayload();
            prediction.setUrticariaType(payload.getUrticariaType());
            prediction.setSecondaryDiseaseRisk(payload.getSecondaryDiseaseRisk());
            prediction.setSideeffectRisk(payload.getSideeffectRisk());
            prediction.setSeverity(payload.getSeverity());
            prediction.setCompositeRiskScore(payload.getCompositeRiskScore());
            prediction.setClinicalInterpretation(payload.getClinicalInterpretation());
            prediction.setModalityGates(payload.getModalityGates());
        }

        if (prescription != null && prescription.getResultPayload() != null) {
            ResultPayload payload = prescription.getResultPayload();
            if (prediction.getMultimodelConfidence() == null) {
                prediction.setMultimodelConfidence(payload.getConfidence());
            }
            if (prediction.getPredictedStep() == null) {
                prediction.setPredictedStep(payload.getMappedGuidelineStep());
            }

            if (prediction.getPredictedDrug() == null) {
                String predictedDrug = payload.getPredictedDrugGroup();
                if (payload.getGuidelineStepDetail() != null
                    && payload.getGuidelineStepDetail().getDrugs() != null
                    && !payload.getGuidelineStepDetail().getDrugs().isEmpty()) {
                    predictedDrug = payload.getGuidelineStepDetail().getDrugs().get(0);
                }
                prediction.setPredictedDrug(predictedDrug);
            }

            if ((prediction.getModalityGates() == null || prediction.getModalityGates().isEmpty())
                && payload.getModalityGateWeights() != null
                && !payload.getModalityGateWeights().isEmpty()) {
                Map<String, Double> gates = new LinkedHashMap<>();
                for (int i = 0; i < payload.getModalityGateWeights().size(); i++) {
                    gates.put("weight_" + i, payload.getModalityGateWeights().get(i));
                }
                prediction.setModalityGates(gates);
            }

            RiskContextSummary riskSummary = payload.getRiskContextSummary();
            if (riskSummary != null) {
                if (prediction.getCompositeRiskScore() == null) {
                    prediction.setCompositeRiskScore(riskSummary.getCompositeRiskScore());
                }
                if (prediction.getClinicalInterpretation() == null) {
                    prediction.setClinicalInterpretation(riskSummary.getClinicalInterpretation());
                }
                if (prediction.getUrticariaType() == null && riskSummary.getUrticariaType() != null) {
                    AiPredictionDocument.UrticariaType urticariaType = new AiPredictionDocument.UrticariaType();
                    urticariaType.setPredicted(riskSummary.getUrticariaType());
                    prediction.setUrticariaType(urticariaType);
                }
                if (prediction.getSeverity() == null
                    && (riskSummary.getSeverityBand() != null || riskSummary.getSeverityScore() != null)) {
                    AiPredictionDocument.Severity severity = new AiPredictionDocument.Severity();
                    severity.setBand(riskSummary.getSeverityBand());
                    severity.setPredictedScore(riskSummary.getSeverityScore());
                    prediction.setSeverity(severity);
                }
                if (prediction.getSideeffectRisk() == null
                    && (riskSummary.getSideeffectLevel() != null || riskSummary.getHighSideeffectFlag() != null)) {
                    AiPredictionDocument.SideEffectRisk sideEffect = new AiPredictionDocument.SideEffectRisk();
                    sideEffect.setLevel(riskSummary.getSideeffectLevel());
                    sideEffect.setHighRiskFlag(riskSummary.getHighSideeffectFlag());
                    prediction.setSideeffectRisk(sideEffect);
                }
                if (prediction.getSecondaryDiseaseRisk() == null
                    && (riskSummary.getThyroidFlag() != null || riskSummary.getAutoimmuneFlag() != null)) {
                    AiPredictionDocument.SecondaryDiseaseRisk secondary = new AiPredictionDocument.SecondaryDiseaseRisk();
                    secondary.setThyroidFlag(riskSummary.getThyroidFlag());
                    secondary.setAutoimmuneFlag(riskSummary.getAutoimmuneFlag());
                    prediction.setSecondaryDiseaseRisk(secondary);
                }
            }
        }

        return prediction;
    }

    private Integer extractScoreTotal(PrescriptionResultDocument prescription, String... keys) {
        Map<String, Double> overrides = labOverridesOf(prescription);
        Double score = firstDouble(overrides, keys);
        return score != null ? score.intValue() : null;
    }

    private Map<String, Double> labOverridesOf(PrescriptionResultDocument prescription) {
        if (prescription == null) {
            return Map.of();
        }
        RequestPayload requestPayload = prescription.getRequestPayload();
        if (requestPayload == null || requestPayload.getLabOverrides() == null) {
            return Map.of();
        }
        return requestPayload.getLabOverrides();
    }

    private Double firstDouble(Map<String, Double> source, String... keys) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        for (String key : keys) {
            if (source.containsKey(key) && source.get(key) != null) {
                return source.get(key);
            }
            for (Map.Entry<String, Double> entry : source.entrySet()) {
                if (entry.getKey() != null
                    && entry.getKey().equalsIgnoreCase(key)
                    && entry.getValue() != null) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String normalizeBlank(String value) {
        return value != null && !value.isBlank() ? value : null;
    }

    // severity.predicted_score (0-10, higher = worse) → UCT (0-16, higher = more controlled)
    private Integer deriveUctFromSeverity(RiskResultDocument risk) {
        if (risk == null || risk.getResultPayload() == null) return null;
        AiPredictionDocument.Severity severity = risk.getResultPayload().getSeverity();
        if (severity == null || severity.getPredictedScore() == null) return null;
        double s = severity.getPredictedScore();
        return Math.max(0, (int) Math.round(16.0 * (1.0 - s / 10.0)));
    }

    // composite_risk_score (0-1, higher = more risk) → AECT (0-16, higher = more controlled)
    private Integer deriveAectFromRisk(RiskResultDocument risk) {
        if (risk == null || risk.getResultPayload() == null) return null;
        Double compositeRisk = risk.getResultPayload().getCompositeRiskScore();
        if (compositeRisk == null) return null;
        return Math.max(0, (int) Math.round(16.0 * (1.0 - compositeRisk)));
    }

    @Getter
    @RequiredArgsConstructor
    public static class CaseContext {
        private final String caseId;
        private final PatientCaseDocument patientCase;
        private final AiPredictionDocument prediction;
        private final Integer uctTotal;
        private final Integer aectTotal;
        private final Instant visitInstant;
        private final String visitDateText;
    }
}
