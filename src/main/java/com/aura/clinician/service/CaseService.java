package com.aura.clinician.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.CaseSummaryResponse;
import com.aura.clinician.domain.CaseInputDocument;
import com.aura.clinician.repository.CaseInputRepository;

@Service
public class CaseService {
    private final CaseInputRepository caseInputRepository;

    public CaseService(CaseInputRepository caseInputRepository) {
        this.caseInputRepository = caseInputRepository;
    }

    public List<CaseSummaryResponse> getCases() {
        return caseInputRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    private CaseSummaryResponse toResponse(CaseInputDocument summary) {
        CaseSummaryResponse response = new CaseSummaryResponse();
        response.setCaseId(summary.getCaseId());
        response.setAge(summary.getPatientAge());
        response.setGender(summary.getPatientGender());
        return response;
    }
}
