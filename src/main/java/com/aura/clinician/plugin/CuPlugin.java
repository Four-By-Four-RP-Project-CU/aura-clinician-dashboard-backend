package com.aura.clinician.plugin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.aura.clinician.api.dto.DashboardResponse;
import com.aura.clinician.api.dto.ExplanationBlock;
import com.aura.clinician.api.dto.PredictionBlock;
import com.aura.clinician.api.dto.RecommendationItem;
import com.aura.clinician.api.dto.ScoreItem;
import com.aura.clinician.provider.ExplainabilityProvider;
import com.aura.clinician.provider.GuidelineProvider;
import com.aura.clinician.provider.PredictionProvider;
import com.aura.clinician.provider.ScoreProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CuPlugin implements DiseaseModule {
    private static final String DISEASE_TYPE = "CU";
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.70;

    private final PredictionProvider predictionProvider;
    private final ScoreProvider scoreProvider;
    private final ExplainabilityProvider explainabilityProvider;
    private final GuidelineProvider guidelineProvider;

    @Override
    public boolean supports(String diseaseType) {
        return DISEASE_TYPE.equalsIgnoreCase(diseaseType);
    }

    @Override
    public DashboardResponse buildDashboard(String caseId) {
        PredictionBlock prediction = predictionProvider.getPrediction(caseId);
        boolean lowConfidence = prediction.getConfidence() < LOW_CONFIDENCE_THRESHOLD;
        prediction.setLowConfidence(lowConfidence);

        List<ScoreItem> scores = scoreProvider.getScores(caseId);
        List<String> warningFlags = new ArrayList<>();

        if (lowConfidence) {
            warningFlags.add("LOW_CONFIDENCE");
        }

        boolean scoreWarning = scores.stream().anyMatch(ScoreItem::isWarning);
        if (scoreWarning) {
            warningFlags.add("CONTROL_SCORE_ALERT");
        }

        ExplanationBlock explanation = explainabilityProvider.getExplanation(caseId);
        List<RecommendationItem> recommendations = guidelineProvider.getRecommendations(caseId, lowConfidence);

        DashboardResponse response = new DashboardResponse();
        response.setCaseId(caseId);
        response.setDiseaseType(DISEASE_TYPE);
        response.setPrediction(prediction);
        response.setScores(scores);
        response.setExplanation(explanation);
        response.setRecommendations(recommendations);
        response.setWarningFlags(warningFlags);
        response.setInterpretationSummary("Moderate disease activity with partial control indicators");

        return response;
    }
}
