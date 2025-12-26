package com.aura.clinician.repository;

import java.util.List;

import com.aura.clinician.domain.AuditEntry;

public interface AuditRepository {
    void append(AuditEntry entry);

    List<AuditEntry> findByCaseId(String caseId);
}
