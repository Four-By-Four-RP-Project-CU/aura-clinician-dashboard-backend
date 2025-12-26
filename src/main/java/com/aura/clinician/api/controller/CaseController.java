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
import com.aura.clinician.domain.CaseDocument;
import com.aura.clinician.service.CaseService;
import com.aura.clinician.service.CaseReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cases")
@Validated
public class CaseController {
    private final CaseService caseService;
    private final CaseReviewService caseReviewService;

    public CaseController(CaseService caseService, CaseReviewService caseReviewService) {
        this.caseService = caseService;
        this.caseReviewService = caseReviewService;
    }

    @GetMapping
    public List<CaseSummaryResponse> getCases() {
        return caseService.getCases();
    }

    @PostMapping("/{caseId}/review")
    public CaseDocument submitReview(
        @PathVariable String caseId,
        @Valid @RequestBody CaseReviewRequest request
    ) {
        return caseReviewService.applyReview(caseId, request.getFinalStatus(), request.getComment());
    }
}
