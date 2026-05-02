package com.aura.clinician.models;

import lombok.Data;

@Data
public class RiskBlockModel {
    private String riskType;
    private String riskLevel;
    private Double riskScore;
}
