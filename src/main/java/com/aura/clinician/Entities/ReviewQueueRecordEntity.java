package com.aura.clinician.Entities;

import com.aura.clinician.Enums.FinalStatus;
import com.aura.clinician.models.RiskBlockModel;
import com.aura.clinician.models.ScoreBlockModel;
import com.aura.clinician.models.ShapScoreModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clinical_reviews")
public class ReviewQueueRecordEntity {

    @Id
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

    /**
     * Stored like 0.91 (0..1 range) in your sample.
     */
    private Double overallConfidenceScore;

    private List<RiskBlockModel> risks;

    private String predictedDrug;
    private String predictedStep;
    private Double confidencePredictedDrugStep;

    private String recommendations;

    @Field("clinicianFinalStatus")
    private FinalStatus clinicianFinalStatus;

    private String comment;

    private Instant createdAt;
    private Instant updatedAt;






}
