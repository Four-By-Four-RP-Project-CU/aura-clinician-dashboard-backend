package com.aura.clinician.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.aura.clinician.domain.CaseSummary;

@Repository
public class InMemoryCaseRepository implements CaseRepository {
    // Mock de-identified case metadata; no PHI stored here.
    private final List<CaseSummary> cases = List.of(
        new CaseSummary("SU-49218", 36, "Female"),
        new CaseSummary("SU-51077", 44, "Male"),
        new CaseSummary("SU-46803", 29, "Female"),
        new CaseSummary("SU-53390", 52, "Male"),
        new CaseSummary("SU-50162", 41, "Female"),
        new CaseSummary("SU-52744", 33, "Male"),
        new CaseSummary("SU-44919", 57, "Female"),
        new CaseSummary("SU-51830", 26, "Male"),
        new CaseSummary("SU-47665", 38, "Female"),
        new CaseSummary("SU-55902", 48, "Male")
    );

    @Override
    public List<CaseSummary> findAll() {
        return cases;
    }
}
