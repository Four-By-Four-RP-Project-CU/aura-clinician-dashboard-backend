package com.aura.clinician.provider.cu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.aura.clinician.api.dto.RecommendationItem;
import com.aura.clinician.provider.GuidelineProvider;

@Component
public class CuGuidelineProvider implements GuidelineProvider {
    @Override
    public List<RecommendationItem> getRecommendations(String caseId, boolean lowConfidence) {
        List<RecommendationItem> recommendations = new ArrayList<>();

        recommendations.add(buildRecommendation(
            "Monitoring",
            "Review symptom diary alongside UCT/AECT to confirm control trends",
            "EAACI/GA2LEN/EDF/WAO"));
        recommendations.add(buildRecommendation(
            "Explainability",
            "Discuss SHAP and Grad-CAM findings with the patient to support shared decisions",
            "EAACI/GA2LEN/EDF/WAO"));

        if (lowConfidence) {
            recommendations.add(buildRecommendation(
                "Data Quality",
                "Low confidence detected; consider additional assessment or repeat imaging",
                "EAACI/GA2LEN/EDF/WAO"));
        }

        return recommendations;
    }

    private RecommendationItem buildRecommendation(String type, String text, String guidelineTag) {
        RecommendationItem item = new RecommendationItem();
        item.setType(type);
        item.setText(text);
        item.setGuidelineTag(guidelineTag);
        return item;
    }
}
