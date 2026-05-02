package com.aura.clinician.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScoreItem {
    private String code;
    private String label;
    private Double value;
    private String interpretation;
    private boolean warning;
}
