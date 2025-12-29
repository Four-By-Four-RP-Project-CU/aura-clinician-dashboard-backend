package com.aura.clinician.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gradcam_outputs")
public class GradcamOutputDocument {
    @Id
    private String id;

    private String caseId;
    private String baseImageUrl;
    private String heatmapUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getBaseImageUrl() {
        return baseImageUrl;
    }

    public void setBaseImageUrl(String baseImageUrl) {
        this.baseImageUrl = baseImageUrl;
    }

    public String getHeatmapUrl() {
        return heatmapUrl;
    }

    public void setHeatmapUrl(String heatmapUrl) {
        this.heatmapUrl = heatmapUrl;
    }
}
