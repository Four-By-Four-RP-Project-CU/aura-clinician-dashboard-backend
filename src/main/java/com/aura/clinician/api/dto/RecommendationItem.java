package com.aura.clinician.api.dto;

public class RecommendationItem {
    private String type;
    private String text;
    private String guidelineTag;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGuidelineTag() {
        return guidelineTag;
    }

    public void setGuidelineTag(String guidelineTag) {
        this.guidelineTag = guidelineTag;
    }
}
