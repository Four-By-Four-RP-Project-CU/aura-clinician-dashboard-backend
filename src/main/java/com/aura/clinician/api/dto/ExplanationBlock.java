package com.aura.clinician.api.dto;

import java.util.ArrayList;
import java.util.List;

public class ExplanationBlock {
    private boolean shapAvailable;
    private boolean gradCamAvailable;
    private List<ShapContribution> shapContributions = new ArrayList<>();
    private GradCamArtifact gradCam;

    public boolean isShapAvailable() {
        return shapAvailable;
    }

    public void setShapAvailable(boolean shapAvailable) {
        this.shapAvailable = shapAvailable;
    }

    public boolean isGradCamAvailable() {
        return gradCamAvailable;
    }

    public void setGradCamAvailable(boolean gradCamAvailable) {
        this.gradCamAvailable = gradCamAvailable;
    }

    public List<ShapContribution> getShapContributions() {
        return shapContributions;
    }

    public void setShapContributions(List<ShapContribution> shapContributions) {
        this.shapContributions = shapContributions;
    }

    public GradCamArtifact getGradCam() {
        return gradCam;
    }

    public void setGradCam(GradCamArtifact gradCam) {
        this.gradCam = gradCam;
    }
}
