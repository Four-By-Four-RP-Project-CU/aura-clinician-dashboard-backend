package com.aura.clinician.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.CaseSummaryResponse;
import com.aura.clinician.domain.CaseSummary;
import com.aura.clinician.repository.CaseRepository;

@Service
public class CaseService {
    private final CaseRepository caseRepository;

    public CaseService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public List<CaseSummaryResponse> getCases() {
        return caseRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private CaseSummaryResponse toResponse(CaseSummary summary) {
        CaseSummaryResponse response = new CaseSummaryResponse();
        response.setCaseId(summary.getCaseId());
        response.setAge(summary.getAge());
        response.setGender(summary.getGender());
        return response;
    }
}
