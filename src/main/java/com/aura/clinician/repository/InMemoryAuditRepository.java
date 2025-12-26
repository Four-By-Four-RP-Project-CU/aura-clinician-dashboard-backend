package com.aura.clinician.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.aura.clinician.domain.AuditEntry;

@Repository
public class InMemoryAuditRepository implements AuditRepository {
    // Stores de-identified audit entries only; no PHI or raw images are persisted here.
    // GDPR/HIPAA note: retention policies and access audits should be enforced in production storage.
    private final Map<String, List<AuditEntry>> auditByCaseId = new ConcurrentHashMap<>();

    @Override
    public void append(AuditEntry entry) {
        auditByCaseId.computeIfAbsent(entry.getCaseId(), key -> new ArrayList<>()).add(entry);
    }

    @Override
    public List<AuditEntry> findByCaseId(String caseId) {
        return auditByCaseId.getOrDefault(caseId, Collections.emptyList());
    }
}
