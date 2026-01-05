package com.aura.clinician.api.dto;

public class JustificationItem {
    private String text;
    private String severity;
    private String guidelineTag;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getGuidelineTag() {
        return guidelineTag;
    }

    public void setGuidelineTag(String guidelineTag) {
        this.guidelineTag = guidelineTag;
    }
}
