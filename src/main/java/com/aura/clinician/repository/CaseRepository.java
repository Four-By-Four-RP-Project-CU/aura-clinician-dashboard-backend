package com.aura.clinician.repository;

import java.util.List;

import com.aura.clinician.domain.CaseSummary;

public interface CaseRepository {
    List<CaseSummary> findAll();
}
