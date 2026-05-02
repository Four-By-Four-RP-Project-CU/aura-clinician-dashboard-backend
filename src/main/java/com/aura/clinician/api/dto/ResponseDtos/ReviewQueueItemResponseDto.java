package com.aura.clinician.api.dto.ResponseDtos;

import com.aura.clinician.Enums.FinalStatus;
import com.aura.clinician.models.RiskBlockModel;
import com.aura.clinician.models.ScoreBlockModel;
import com.aura.clinician.models.ShapScoreModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewQueueItemResponseDto {
    private String id;
    private String caseId;

    private Integer patientAge;
    private String patientGender;
    private String hospital;

    private Instant visitDate;
    private String symptoms;
    private String urticariaType;

    private ScoreBlockModel uct;
    private ScoreBlockModel aect;

    private Boolean shapAvailable;
    private Boolean gradCamAvailable;

    private String gradCamHeatMapImage;
    private String images;

    private List<ShapScoreModel> shapScores;

    private Double overallConfidenceScore;

    private List<RiskBlockModel> risks;

    private String predictedDrug;
    private String predictedStep;
    private Double confidencePredictedDrugStep;

    private String recommendations;

    private FinalStatus clinicianFinalStatus;
    private String comment;

    private Instant createdAt;
    private Instant updatedAt;
}
