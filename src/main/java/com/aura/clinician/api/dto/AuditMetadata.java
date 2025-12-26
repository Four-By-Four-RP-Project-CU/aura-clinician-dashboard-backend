package com.aura.clinician.api.dto;

import java.time.Instant;

public class AuditMetadata {
    private String modelVersion;
    private Instant timestamp;
    private boolean explainabilityEnabled;
    private String dataSource;
    private String auditId;

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isExplainabilityEnabled() {
        return explainabilityEnabled;
    }

    public void setExplainabilityEnabled(boolean explainabilityEnabled) {
        this.explainabilityEnabled = explainabilityEnabled;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getAuditId() {
        return auditId;
    }

    public void setAuditId(String auditId) {
        this.auditId = auditId;
    }
}
