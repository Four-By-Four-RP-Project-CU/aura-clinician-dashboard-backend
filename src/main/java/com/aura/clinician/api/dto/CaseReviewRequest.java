package com.aura.clinician.api.dto;

import jakarta.validation.constraints.NotBlank;

public class CaseReviewRequest {
    @NotBlank
    private String finalStatus;

    private String comment;

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
