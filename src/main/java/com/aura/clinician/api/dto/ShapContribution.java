package com.aura.clinician.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShapContribution {
    private String feature;
    private double contribution;
    private String direction;
}
