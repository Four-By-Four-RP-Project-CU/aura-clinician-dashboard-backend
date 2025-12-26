package com.aura.clinician.repository;

import java.util.List;

import com.aura.clinician.domain.FeedbackEntry;

public interface FeedbackRepository {
    void append(FeedbackEntry entry);

    List<FeedbackEntry> findByCaseId(String caseId);
}
