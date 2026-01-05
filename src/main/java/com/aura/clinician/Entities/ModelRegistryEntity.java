package com.aura.clinician.Entities;

import com.aura.clinician.Enums.TrainingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "model_registry")
public class ModelRegistryEntity {

    @Id
    private String id;

    private String modelVersion;

    private TrainingStatus status;

    private Instant createdAt;

    private Integer trainedCases;


    private Map<String, Object> metrics;
    private List<String> featureColumns;

    private Instant callbackUpdatedAt;

    private boolean promoted;
}
