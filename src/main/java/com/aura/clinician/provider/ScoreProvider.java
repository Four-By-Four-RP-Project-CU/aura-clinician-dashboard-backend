package com.aura.clinician.provider;

import java.util.List;

import com.aura.clinician.api.dto.ScoreItem;

public interface ScoreProvider {
    List<ScoreItem> getScores(String caseId);
}
