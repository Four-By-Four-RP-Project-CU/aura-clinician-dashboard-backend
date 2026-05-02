package com.aura.clinician.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.CaseSummaryResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaseService {
    private static final Logger logger = LoggerFactory.getLogger(CaseService.class);
    private final CaseContextService caseContextService;

    public List<CaseSummaryResponse> getCases() {
        List<CaseContextService.CaseContext> cases = caseContextService.getAllCaseContexts();
        logger.info("Loaded {} case contexts from risk_results/prescription_results", cases.size());
        return cases.stream()
            .map(this::toResponse)
            .toList();
    }

    private CaseSummaryResponse toResponse(CaseContextService.CaseContext context) {
        CaseSummaryResponse response = new CaseSummaryResponse();
        response.setCaseId(context.getCaseId());
        response.setAge(context.getPatientCase() != null ? context.getPatientCase().getAgeYears() : null);
        response.setGender(
            context.getPatientCase() != null
                && context.getPatientCase().getSex() != null
                && !context.getPatientCase().getSex().isBlank()
                    ? context.getPatientCase().getSex()
                    : "Unknown"
        );
        return response;
    }
}
