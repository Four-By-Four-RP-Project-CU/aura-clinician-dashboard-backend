package com.aura.clinician.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.aura.clinician.domain.FeedbackEntry;

@Repository
public class InMemoryFeedbackRepository implements FeedbackRepository {
    // Feedback is stored in-memory for demo; production storage should encrypt data at rest.
    private final Map<String, List<FeedbackEntry>> feedbackByCaseId = new ConcurrentHashMap<>();

    @Override
    public void append(FeedbackEntry entry) {
        feedbackByCaseId.computeIfAbsent(entry.getCaseId(), key -> new ArrayList<>()).add(entry);
    }

    @Override
    public List<FeedbackEntry> findByCaseId(String caseId) {
        return feedbackByCaseId.getOrDefault(caseId, Collections.emptyList());
    }
}
