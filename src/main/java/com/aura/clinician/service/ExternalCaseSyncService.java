package com.aura.clinician.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.aura.clinician.domain.AiPredictionDocument;
import com.aura.clinician.domain.PatientCaseDocument;
import com.aura.clinician.domain.PrescriptionResultDocument;
import com.aura.clinician.domain.PrescriptionResultDocument.ResultPayload;
import com.aura.clinician.domain.PrescriptionResultDocument.RiskContextSummary;
import com.aura.clinician.domain.RiskResultDocument;
import com.aura.clinician.repository.AiPredictionRepository;
import com.aura.clinician.repository.PatientCaseRepository;
import com.aura.clinician.repository.PrescriptionResultRepository;
import com.aura.clinician.repository.RiskResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExternalCaseSyncService {
    private static final Logger logger = LoggerFactory.getLogger(ExternalCaseSyncService.class);

    private final PrescriptionResultRepository prescriptionResultRepository;
    private final RiskResultRepository riskResultRepository;
    private final PatientCaseRepository patientCaseRepository;
    private final AiPredictionRepository aiPredictionRepository;

    @Value("${clinician.base-url:http://localhost:8081}")
    private String clinicianBaseUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        try {
            syncCases();
        } catch (Exception ex) {
            logger.error("External case sync failed on startup: {}", ex.getMessage(), ex);
        }
    }

    public void syncCases() {
        syncPatientCases();
        syncAiPredictions();
    }

    private void syncPatientCases() {
        List<PrescriptionResultDocument> prescriptions = prescriptionResultRepository.findAll();
        logger.info("Found {} prescription_results records to sync", prescriptions.size());
        int synced = 0;
        for (PrescriptionResultDocument src : prescriptions) {
            if (src.getCaseId() == null || src.getCaseId().isBlank()) {
                continue;
            }
            if (patientCaseRepository.findByCaseId(src.getCaseId()).isPresent()) {
                logger.debug("Skipping existing patient case caseId={}", src.getCaseId());
                continue;
            }
            patientCaseRepository.save(toPatientCase(src));
            synced++;
        }
        logger.info("Synced {} new patient_cases from prescription_results", synced);
    }

    private void syncAiPredictions() {
        // Sync from risk_results if available
        List<RiskResultDocument> riskResults = riskResultRepository.findAll();
        logger.info("Found {} risk_results records to sync", riskResults.size());
        int synced = 0;
        for (RiskResultDocument src : riskResults) {
            if (src.getCaseId() == null || src.getCaseId().isBlank()) {
                continue;
            }
            if (aiPredictionRepository.findByCaseId(src.getCaseId()).isPresent()) {
                logger.debug("Skipping existing ai_prediction caseId={}", src.getCaseId());
                continue;
            }
            aiPredictionRepository.save(toPrediction(src));
            synced++;
        }

        // Also sync from prescription_results for cases not yet covered
        List<PrescriptionResultDocument> prescriptions = prescriptionResultRepository.findAll();
        for (PrescriptionResultDocument src : prescriptions) {
            if (src.getCaseId() == null || src.getCaseId().isBlank()) {
                continue;
            }
            if (aiPredictionRepository.findByCaseId(src.getCaseId()).isPresent()) {
                logger.debug("Skipping existing ai_prediction (prescription) caseId={}", src.getCaseId());
                continue;
            }
            aiPredictionRepository.save(toPredictionFromPrescription(src));
            synced++;
        }
        logger.info("Synced {} new ai_predictions total", synced);
    }

    private PatientCaseDocument toPatientCase(PrescriptionResultDocument src) {
        PatientCaseDocument doc = new PatientCaseDocument();
        doc.setCaseId(src.getCaseId());
        doc.setDiseaseType("CU");
        doc.setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt() : Instant.now());

        // Image from GridFS via asset_refs where kind=input_asset
        String fileId = src.getInputAssetFileId();
        if (fileId != null) {
            doc.setImagePath(clinicianBaseUrl + "/api/v1/images/gridfs/" + fileId);
        }

        // Labs and age come from request_payload.lab_overrides
        Map<String, Double> labOverrides = src.getRequestPayload() != null
            ? src.getRequestPayload().getLabOverrides()
            : null;

        if (labOverrides != null && !labOverrides.isEmpty()) {
            PatientCaseDocument.Labs labs = new PatientCaseDocument.Labs();
            labs.setCrp(labOverrides.get("CRP"));
            labs.setFt4(labOverrides.get("FT4"));
            labs.setIgE(labOverrides.get("IgE"));
            labs.setVitD(labOverrides.get("VitD"));
            doc.setLabs(labs);

            Double age = labOverrides.get("Age");
            doc.setAgeYears(age != null ? age.intValue() : null);
        }

        // sex is derived from risk_results at sync time; left null here since
        // CaseContextService.buildPatientCase resolves it live from risk.resolvedSex()

        return doc;
    }

    private AiPredictionDocument toPredictionFromPrescription(PrescriptionResultDocument src) {
        AiPredictionDocument doc = new AiPredictionDocument();
        doc.setCaseId(src.getCaseId());
        doc.setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt() : Instant.now());

        ResultPayload payload = src.getResultPayload();
        if (payload == null) {
            return doc;
        }

        doc.setMultimodelConfidence(payload.getConfidence());
        doc.setPredictedStep(payload.getMappedGuidelineStep());

        if (payload.getGuidelineStepDetail() != null) {
            List<String> drugs = payload.getGuidelineStepDetail().getDrugs();
            if (drugs != null && !drugs.isEmpty()) {
                doc.setPredictedDrug(drugs.get(0));
            } else {
                doc.setPredictedDrug(payload.getPredictedDrugGroup());
            }
        } else {
            doc.setPredictedDrug(payload.getPredictedDrugGroup());
        }

        // Modality gate weights (list index → named key)
        List<Double> weights = payload.getModalityGateWeights();
        if (weights != null && !weights.isEmpty()) {
            Map<String, Double> gates = new LinkedHashMap<>();
            for (int i = 0; i < weights.size(); i++) {
                gates.put("weight_" + i, weights.get(i));
            }
            doc.setModalityGates(gates);
        }

        RiskContextSummary risk = payload.getRiskContextSummary();
        if (risk == null) {
            return doc;
        }

        doc.setCompositeRiskScore(risk.getCompositeRiskScore());
        doc.setClinicalInterpretation(risk.getClinicalInterpretation());

        // Urticaria type
        AiPredictionDocument.UrticariaType uct = new AiPredictionDocument.UrticariaType();
        uct.setPredicted(risk.getUrticariaType());
        doc.setUrticariaType(uct);

        // Severity
        AiPredictionDocument.Severity severity = new AiPredictionDocument.Severity();
        severity.setBand(risk.getSeverityBand());
        severity.setPredictedScore(risk.getSeverityScore());
        doc.setSeverity(severity);

        // Side effect risk
        AiPredictionDocument.SideEffectRisk sideEffect = new AiPredictionDocument.SideEffectRisk();
        sideEffect.setLevel(risk.getSideeffectLevel());
        sideEffect.setHighRiskFlag(risk.getHighSideeffectFlag());
        doc.setSideeffectRisk(sideEffect);

        // Secondary disease risk
        AiPredictionDocument.SecondaryDiseaseRisk secondary = new AiPredictionDocument.SecondaryDiseaseRisk();
        secondary.setThyroidFlag(risk.getThyroidFlag());
        secondary.setAutoimmuneFlag(risk.getAutoimmuneFlag());
        doc.setSecondaryDiseaseRisk(secondary);

        return doc;
    }

    private AiPredictionDocument toPrediction(RiskResultDocument src) {
        AiPredictionDocument doc = new AiPredictionDocument();
        doc.setCaseId(src.getCaseId());
        doc.setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt() : Instant.now());

        RiskResultDocument.ResultPayload payload = src.getResultPayload();
        if (payload == null) {
            return doc;
        }

        doc.setUrticariaType(payload.getUrticariaType());
        doc.setSecondaryDiseaseRisk(payload.getSecondaryDiseaseRisk());
        doc.setSideeffectRisk(payload.getSideeffectRisk());
        doc.setSeverity(payload.getSeverity());
        doc.setCompositeRiskScore(payload.getCompositeRiskScore());
        doc.setClinicalInterpretation(payload.getClinicalInterpretation());
        doc.setModalityGates(payload.getModalityGates());

        return doc;
    }
}
