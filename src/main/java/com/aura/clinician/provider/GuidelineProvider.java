package com.aura.clinician.provider;

import java.util.List;

import com.aura.clinician.api.dto.RecommendationItem;

public interface GuidelineProvider {
    List<RecommendationItem> getRecommendations(String caseId, boolean lowConfidence);
}
