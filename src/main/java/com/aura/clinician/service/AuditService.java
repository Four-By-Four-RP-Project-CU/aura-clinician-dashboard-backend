package com.aura.clinician.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.aura.clinician.domain.AuditEntry;
import com.aura.clinician.repository.AuditRepository;

@Service
public class AuditService {
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public String logDashboardView(String caseId, String diseaseType, String actor) {
        AuditEntry entry = baseEntry(caseId, diseaseType, actor);
        entry.setAction("DASHBOARD_VIEW");
        entry.setDetails("Dashboard generated for clinician review");
        auditRepository.append(entry);
        return entry.getId();
    }

    public void logFeedback(String caseId, String diseaseType, String actor, String action) {
        AuditEntry entry = baseEntry(caseId, diseaseType, actor);
        entry.setAction("FEEDBACK_RECEIVED");
        entry.setDetails("Clinician review action: " + action);
        auditRepository.append(entry);
    }

    public List<AuditEntry> getAuditTrail(String caseId) {
        return auditRepository.findByCaseId(caseId);
    }

    private AuditEntry baseEntry(String caseId, String diseaseType, String actor) {
        AuditEntry entry = new AuditEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setCaseId(caseId);
        entry.setDiseaseType(diseaseType);
        entry.setActor(actor);
        entry.setTimestamp(Instant.now());
        entry.getTags().add("DE_IDENTIFIED");
        return entry;
    }
}
