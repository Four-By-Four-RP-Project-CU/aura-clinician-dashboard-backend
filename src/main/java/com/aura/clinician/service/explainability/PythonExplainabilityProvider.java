package com.aura.clinician.service.explainability;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.GradCamArtifact;
import com.aura.clinician.api.dto.ShapContribution;
import com.aura.clinician.domain.PatientCaseDocument;

@Service
@Profile("python")
public class PythonExplainabilityProvider implements ExplainabilityProvider {

    @Override
    public List<ShapContribution> getShap(String caseId, PatientCaseDocument patientCase) {
        return Collections.emptyList();
    }

    @Override
    public GradCamArtifact getGradcam(String caseId, PatientCaseDocument patientCase) {
        return null;
    }
}
