package com.aura.clinician.api.dto.RequestDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelTrainingCallbackRequestDto {

    private String status;

    private String version;

    private String message;

    private Map<String, Object> metrics;

    private String reason;
}
