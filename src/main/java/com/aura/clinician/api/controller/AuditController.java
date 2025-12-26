package com.aura.clinician.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aura.clinician.domain.AuditEntry;
import com.aura.clinician.service.AuditService;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/{caseId}")
    public List<AuditEntry> getAuditTrail(@PathVariable String caseId) {
        return auditService.getAuditTrail(caseId);
    }
}
