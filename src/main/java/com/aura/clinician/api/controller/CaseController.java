package com.aura.clinician.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aura.clinician.api.dto.CaseSummaryResponse;
import com.aura.clinician.service.CaseService;

@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {
    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping
    public List<CaseSummaryResponse> getCases() {
        return caseService.getCases();
    }
}
