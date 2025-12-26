package com.aura.clinician.service;

import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.DashboardResponse;
import com.aura.clinician.plugin.DiseaseModule;
import com.aura.clinician.plugin.DiseaseModuleRegistry;

@Service
public class DashboardService {
    private final DiseaseModuleRegistry registry;
    private final AuditService auditService;

    public DashboardService(DiseaseModuleRegistry registry, AuditService auditService) {
        this.registry = registry;
        this.auditService = auditService;
    }

    public DashboardResponse getDashboard(String caseId, String diseaseType, String actor) {
        DiseaseModule module = registry.getModule(diseaseType);
        DashboardResponse response = module.buildDashboard(caseId);
        String auditId = auditService.logDashboardView(caseId, diseaseType, actor);
        if (response.getAudit() != null) {
            response.getAudit().setAuditId(auditId);
        }
        return response;
    }
}
