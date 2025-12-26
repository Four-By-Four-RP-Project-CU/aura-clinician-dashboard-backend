package com.aura.clinician.api.controller;

import java.security.Principal;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aura.clinician.api.dto.DashboardResponse;
import com.aura.clinician.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboard")
@Validated
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{caseId}")
    public DashboardResponse getDashboard(
        @PathVariable String caseId,
        @RequestParam String diseaseType,
        Principal principal
    ) {
        String actor = principal != null ? principal.getName() : "system";
        return dashboardService.getDashboard(caseId, diseaseType, actor);
    }
}
