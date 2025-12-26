package com.aura.clinician.plugin;

import com.aura.clinician.api.dto.DashboardResponse;

public interface DiseaseModule {
    boolean supports(String diseaseType);

    DashboardResponse buildDashboard(String caseId);
}
