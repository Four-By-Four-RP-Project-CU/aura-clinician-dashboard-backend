package com.aura.clinician.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.JustificationItem;
import com.aura.clinician.domain.AiPredictionDocument;

@Service
public class JustificationService {
    private static final String DEFAULT_GUIDELINE = "EAACI";

    public List<JustificationItem> buildJustifications(
        int uctTotal,
        int aectTotal,
        AiPredictionDocument prediction
    ) {
        List<JustificationItem> justifications = new ArrayList<>();

        if (uctTotal < 12) {
            justifications.add(build("Poor disease control indicated by UCT score.", "CAUTION"));
        }
        if (aectTotal < 10) {
            justifications.add(build("Angioedema control is below target per AECT score.", "CAUTION"));
        }

        if (prediction != null) {
            if ("Step-Up".equalsIgnoreCase(prediction.getPredictedStep())
                && riskLevelEquals(prediction.getRisks(), "sideEffect", "HIGH")) {
                justifications.add(build("High side-effect risk suggests caution during escalation.", "CAUTION"));
            }

            Double confidence = prediction.getConfidence();
            if (confidence != null && confidence < 0.7) {
                justifications.add(build("Low prediction confidence. Specialist review recommended.", "REVIEW"));
            }

            Double uncertainty = prediction.getUncertainty();
            if (uncertainty != null && uncertainty > 0.4) {
                justifications.add(build("Prediction uncertainty is elevated. Review before clinical action.", "REVIEW"));
            }
        }

        return justifications;
    }

    private JustificationItem build(String text, String severity) {
        JustificationItem item = new JustificationItem();
        item.setText(text);
        item.setSeverity(severity);
        item.setGuidelineTag(DEFAULT_GUIDELINE);
        return item;
    }

    private boolean riskLevelEquals(AiPredictionDocument.Risks risks, String riskType, String expected) {
        if (risks == null) {
            return false;
        }
        AiPredictionDocument.RiskItem item = switch (riskType) {
            case "sideEffect" -> risks.getSideEffect();
            case "hypersensitivity" -> risks.getHypersensitivity();
            case "secondaryDisease" -> risks.getSecondaryDisease();
            default -> null;
        };
        return item != null && item.getLevel() != null && item.getLevel().equalsIgnoreCase(expected);
    }
}
