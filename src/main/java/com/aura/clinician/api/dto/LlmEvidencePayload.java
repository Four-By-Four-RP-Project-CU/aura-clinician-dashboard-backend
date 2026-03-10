package com.aura.clinician.api.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LlmEvidencePayload {
    private String subtypePredictionLabel;
    private Double confidence;

    private Double uctScore;
    private String uctStatus;

    private Double aectScore;
    private String aectStatus;

    private List<ShapContribution> topShapFeatures = new ArrayList<>();

    private String gradCamSummary;
    private String gradCamHeatmapUrl;
    private String gradCamBaseImageUrl;

    private List<String> recommendations = new ArrayList<>();
    private List<String> treatmentPathwayList = new ArrayList<>();
    private List<String> missingData = new ArrayList<>();
}
