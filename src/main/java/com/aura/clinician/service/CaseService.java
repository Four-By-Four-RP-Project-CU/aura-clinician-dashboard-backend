package com.aura.clinician.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.CaseSummaryResponse;
import com.aura.clinician.domain.PatientCaseDocument;
import com.aura.clinician.repository.PatientCaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaseService {
    private static final Logger logger = LoggerFactory.getLogger(CaseService.class);
    private final PatientCaseRepository patientCaseRepository;

    public List<CaseSummaryResponse> getCases() {
        List<PatientCaseDocument> cases = patientCaseRepository.findAll();
        logger.info("Loaded {} patient_cases records", cases.size());
        return cases.stream()
            .map(this::toResponse)
            .toList();
    }

    private CaseSummaryResponse toResponse(PatientCaseDocument summary) {
        CaseSummaryResponse response = new CaseSummaryResponse();
        response.setCaseId(summary.getCaseId());
        response.setAge(summary.getAgeYears());
        response.setGender(summary.getSex());
        return response;
    }
}
