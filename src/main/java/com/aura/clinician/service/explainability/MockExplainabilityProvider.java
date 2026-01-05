package com.aura.clinician.service.explainability;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.GradCamArtifact;
import com.aura.clinician.api.dto.ShapContribution;
import com.aura.clinician.domain.PatientCaseDocument;

@Service
@Profile("!python")
public class MockExplainabilityProvider implements ExplainabilityProvider {
    private static final String MOCK_BASE_IMAGE = "/images/clinician-base.png";
    private static final String MOCK_HEATMAP_IMAGE = "/images/clinician-hm.png";

    @Override
    public List<ShapContribution> getShap(String caseId, PatientCaseDocument patientCase) {
        List<ShapContribution> contributions = new ArrayList<>();
        if (patientCase == null) {
            return contributions;
        }

        PatientCaseDocument.Labs labs = patientCase.getLabs();
        if (labs != null) {
            addContribution(contributions, "IgE", scale(labs.getIgE(), 1000.0));
            addContribution(contributions, "CRP", scale(labs.getCrp(), 20.0));
            addContribution(contributions, "VitD", invertScale(labs.getVitD(), 40.0));
        }

        PatientCaseDocument.Symptoms symptoms = patientCase.getSymptoms();
        if (symptoms != null) {
            addContribution(contributions, "Itching Score", scale(parseNumeric(symptoms.getItchingScore()), 4.0));
            addContribution(contributions, "Angioedema Duration (hrs)", scale(parseNumeric(symptoms.getAngioedemaDuration()), 48.0));
        }

        Integer uctTotal = sumScores(patientCase.getUct());
        if (uctTotal != null) {
            addContribution(contributions, "UCT Total", invertScale(uctTotal.doubleValue(), 16.0));
        }

        return contributions;
    }

    @Override
    public GradCamArtifact getGradcam(String caseId, PatientCaseDocument patientCase) {
        GradCamArtifact artifact = new GradCamArtifact();
        artifact.setBaseImageUrl(MOCK_BASE_IMAGE);
        artifact.setHeatmapUrl(MOCK_HEATMAP_IMAGE);
        return artifact;
    }

    private Integer sumScores(PatientCaseDocument.QuestionnaireScore score) {
        if (score == null || score.getQ1() == null || score.getQ2() == null || score.getQ3() == null || score.getQ4() == null) {
            return null;
        }
        return score.getQ1() + score.getQ2() + score.getQ3() + score.getQ4();
    }

    private Double parseNumeric(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.trim().replaceAll("[^0-9.]", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void addContribution(List<ShapContribution> contributions, String feature, Double value) {
        if (value == null) {
            return;
        }
        ShapContribution item = new ShapContribution();
        item.setFeature(feature);
        item.setContribution(round(value));
        item.setDirection(value >= 0 ? "POSITIVE" : "NEGATIVE");
        contributions.add(item);
    }

    private Double scale(Double value, double max) {
        if (value == null) {
            return null;
        }
        double normalized = Math.min(value / max, 1.0);
        return normalized;
    }

    private Double invertScale(Double value, double max) {
        if (value == null) {
            return null;
        }
        double normalized = 1.0 - Math.min(value / max, 1.0);
        return normalized;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
