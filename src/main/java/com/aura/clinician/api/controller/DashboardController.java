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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@Validated
@RequiredArgsConstructor
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final DashboardService dashboardService;

    @GetMapping("/{caseId}")
    public DashboardResponse getDashboard(
        @PathVariable String caseId,
        @RequestParam String diseaseType,
        @RequestParam(defaultValue = "false") boolean includeExplainability
    ) {
        logger.info(
            "HIT - /api/v1/dashboard/{} | req diseaseType={} includeExplainability={}",
            caseId,
            diseaseType,
            includeExplainability
        );
        return dashboardService.getDashboard(caseId, diseaseType, includeExplainability);
    }
}
