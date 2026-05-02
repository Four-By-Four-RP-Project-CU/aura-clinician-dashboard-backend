package com.aura.clinician.Entities;

import com.aura.clinician.Enums.ClinicianFinalDecision;
import com.aura.clinician.Enums.FinalStatus;
import com.aura.clinician.models.RiskBlockModel;
import com.aura.clinician.models.ScoreBlockModel;
import com.aura.clinician.models.ShapScoreModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clinical_feedback")
public class ClinicianFeedbackEntity {

    @Id
    private String id;

    private String caseId;

    private Integer patientAge;
    private String patientGender;

    private ScoreBlockModel uct;
    private ScoreBlockModel aect;


    private String predictedDrug;
    private String predictedStep;
    private Double confidencePredictedDrugStep;

    private String correctedDrug;
    private String correctedStep;


    @Field("clinicianFinalDecision")
    private ClinicianFinalDecision clinicianFinalDecision;

    private boolean trained;

    private String comment;

    private String reviewedBy;

    private Instant createdAt;
    private Instant updatedAt;






}
