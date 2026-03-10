package com.aura.clinician.api.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aura.clinician.api.dto.CaseReviewRequest;
import com.aura.clinician.api.dto.CaseSummaryResponse;
import com.aura.clinician.domain.ClinicalReviewDocument;
import com.aura.clinician.service.CaseService;
import com.aura.clinician.service.CaseReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cases")
@Validated
@RequiredArgsConstructor
public class CaseController {
    private static final Logger logger = LoggerFactory.getLogger(CaseController.class);
    private final CaseService caseService;
    private final CaseReviewService caseReviewService;

    @GetMapping
    public List<CaseSummaryResponse> getCases() {
        logger.info("HIT - /api/v1/cases | req");
        return caseService.getCases();
    }

    @PostMapping("/{caseId}/review/{finalStatus}")
    public ClinicalReviewDocument submitReview(
        @PathVariable String caseId,
        @PathVariable String finalStatus,
        @Valid @RequestBody CaseReviewRequest request
    ) {
        logger.info("HIT - /api/v1/cases/{}/review/{} | req", caseId, finalStatus);
        return caseReviewService.applyReview(caseId, finalStatus, request.getComment());
    }
}
