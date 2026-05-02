package com.aura.clinician.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecommendationItem {
    private String type;
    private String text;
    private String guidelineTag;
    private String severity;
}
