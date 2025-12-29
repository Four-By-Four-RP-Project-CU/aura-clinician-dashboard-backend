package com.aura.clinician.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{caseId}")
    public DashboardResponse getDashboard(
        @PathVariable String caseId,
        @RequestParam String diseaseType
    ) {
        logger.info("HIT - /api/v1/dashboard/{} | req diseaseType={}", caseId, diseaseType);
        return dashboardService.getDashboard(caseId, diseaseType);
    }
}
