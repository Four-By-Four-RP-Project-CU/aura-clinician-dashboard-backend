package com.aura.clinician.api.dto.ResponseDtos;

import com.aura.clinician.Enums.TrainingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelRegistryItemResponseDto {
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
