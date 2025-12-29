package com.aura.clinician.service.explainability;

import java.util.List;

import com.aura.clinician.api.dto.GradCamArtifact;
import com.aura.clinician.api.dto.ShapContribution;
import com.aura.clinician.domain.PatientCaseDocument;

public interface ExplainabilityProvider {
    List<ShapContribution> getShap(String caseId, PatientCaseDocument patientCase);

    GradCamArtifact getGradcam(String caseId, PatientCaseDocument patientCase);
}
