package com.aura.clinician.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JustificationItem {
    private String text;
    private String severity;
    private String guidelineTag;
}
