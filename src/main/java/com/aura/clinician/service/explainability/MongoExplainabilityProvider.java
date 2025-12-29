package com.aura.clinician.service.explainability;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.GradCamArtifact;
import com.aura.clinician.api.dto.ShapContribution;
import com.aura.clinician.domain.GradcamOutputDocument;
import com.aura.clinician.domain.PatientCaseDocument;
import com.aura.clinician.domain.ShapExplanationDocument;
import com.aura.clinician.repository.GradcamOutputRepository;
import com.aura.clinician.repository.ShapExplanationRepository;

@Service
@Profile("mongo-explainability")
public class MongoExplainabilityProvider implements ExplainabilityProvider {
    private final ShapExplanationRepository shapRepository;
    private final GradcamOutputRepository gradcamRepository;

    public MongoExplainabilityProvider(
        ShapExplanationRepository shapRepository,
        GradcamOutputRepository gradcamRepository
    ) {
        this.shapRepository = shapRepository;
        this.gradcamRepository = gradcamRepository;
    }

    @Override
    public List<ShapContribution> getShap(String caseId, PatientCaseDocument patientCase) {
        List<ShapContribution> contributions = new ArrayList<>();
        ShapExplanationDocument shap = shapRepository.findByCaseId(caseId).orElse(null);
        if (shap != null && shap.getFeatures() != null) {
            for (ShapExplanationDocument.ShapFeature feature : shap.getFeatures()) {
                ShapContribution item = new ShapContribution();
                item.setFeature(feature.getFeature());
                item.setContribution(feature.getContribution());
                item.setDirection(feature.getDirection());
                contributions.add(item);
            }
        }
        return contributions;
    }

    @Override
    public GradCamArtifact getGradcam(String caseId, PatientCaseDocument patientCase) {
        GradCamArtifact artifact = null;
        GradcamOutputDocument gradcam = gradcamRepository.findByCaseId(caseId).orElse(null);
        if (gradcam != null) {
            artifact = new GradCamArtifact();
            artifact.setBaseImageUrl(gradcam.getBaseImageUrl());
            artifact.setHeatmapUrl(gradcam.getHeatmapUrl());
        }
        return artifact;
    }
}
