package com.aura.clinician.service;

public class GuidelineContext {
    private final int uctTotal;
    private final int aectTotal;
    private final Double confidence;
    private final String predictedStep;
    private final String sideEffectLevel;
    private final Double sideEffectScore;
    private final String hypersensitivityLevel;
    private final Double hypersensitivityScore;
    private final String secondaryDiseaseLevel;
    private final Double secondaryDiseaseScore;
    private final String dailyActivityImpact;

    public GuidelineContext(
        int uctTotal,
        int aectTotal,
        Double confidence,
        String predictedStep,
        String sideEffectLevel,
        Double sideEffectScore,
        String hypersensitivityLevel,
        Double hypersensitivityScore,
        String secondaryDiseaseLevel,
        Double secondaryDiseaseScore,
        String dailyActivityImpact
    ) {
        this.uctTotal = uctTotal;
        this.aectTotal = aectTotal;
        this.confidence = confidence;
        this.predictedStep = predictedStep;
        this.sideEffectLevel = sideEffectLevel;
        this.sideEffectScore = sideEffectScore;
        this.hypersensitivityLevel = hypersensitivityLevel;
        this.hypersensitivityScore = hypersensitivityScore;
        this.secondaryDiseaseLevel = secondaryDiseaseLevel;
        this.secondaryDiseaseScore = secondaryDiseaseScore;
        this.dailyActivityImpact = dailyActivityImpact;
    }

    public Object resolve(String key) {
        return switch (key) {
            case "uctTotal" -> uctTotal;
            case "UCT_total" -> uctTotal;
            case "aectTotal" -> aectTotal;
            case "AECT_total" -> aectTotal;
            case "confidence" -> confidence;
            case "predictedStep" -> predictedStep;
            case "sideEffectRisk" -> sideEffectLevel;
            case "hypersensitivityRisk" -> hypersensitivityLevel;
            case "secondaryDiseaseRisk" -> secondaryDiseaseLevel;
            case "risk.sideEffect.level" -> sideEffectLevel;
            case "risk.sideEffect.score" -> sideEffectScore;
            case "risk.hypersensitivity.level" -> hypersensitivityLevel;
            case "risk.hypersensitivity.score" -> hypersensitivityScore;
            case "risk.secondaryDisease.level" -> secondaryDiseaseLevel;
            case "risk.secondaryDisease.score" -> secondaryDiseaseScore;
            case "dailyActivityImpact" -> dailyActivityImpact;
            default -> null;
        };
    }
}
