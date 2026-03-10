package com.aura.clinician.api.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExplanationBlock {
    private boolean shapAvailable;
    private boolean gradCamAvailable;
    private List<ShapContribution> shapContributions = new ArrayList<>();
    private GradCamArtifact gradCam;
}
