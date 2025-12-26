package com.aura.clinician.provider.cu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.aura.clinician.api.dto.ExplanationBlock;
import com.aura.clinician.api.dto.GradCamArtifact;
import com.aura.clinician.api.dto.ShapContribution;
import com.aura.clinician.provider.ExplainabilityProvider;

@Component
public class CuExplainabilityProvider implements ExplainabilityProvider {
    @Override
    public ExplanationBlock getExplanation(String caseId) {
        ExplanationBlock block = new ExplanationBlock();
        block.setShapAvailable(true);
        block.setGradCamAvailable(true);

        List<ShapContribution> contributions = new ArrayList<>();
        contributions.add(buildContribution("Wheal count", 0.42, "positive"));
        contributions.add(buildContribution("Itch severity", 0.31, "positive"));
        contributions.add(buildContribution("Trigger exposure", -0.18, "negative"));
        block.setShapContributions(contributions);

        GradCamArtifact gradCam = new GradCamArtifact();
        gradCam.setBaseImageUrl("https://example.org/artifacts/cu/base-image.png");
        gradCam.setHeatmapUrl("https://example.org/artifacts/cu/gradcam-overlay.png");
        block.setGradCam(gradCam);

        return block;
    }

    private ShapContribution buildContribution(String feature, double contribution, String direction) {
        ShapContribution item = new ShapContribution();
        item.setFeature(feature);
        item.setContribution(contribution);
        item.setDirection(direction);
        return item;
    }
}
