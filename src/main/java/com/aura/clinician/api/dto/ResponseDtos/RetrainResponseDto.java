package com.aura.clinician.api.dto.ResponseDtos;

import com.aura.clinician.Enums.TrainingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrainResponseDto {
    private TrainingStatus status;
    private String version;
    private String message;
}
