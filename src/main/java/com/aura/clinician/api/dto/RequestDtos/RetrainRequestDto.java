package com.aura.clinician.api.dto.RequestDtos;

import com.aura.clinician.Enums.ClinicianFinalDecision;
import com.aura.clinician.api.dto.RetrainCaseDto;
import com.aura.clinician.models.ScoreBlockModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RetrainRequestDto {
    private int min_new;
    private boolean force;
    private List<RetrainCaseDto> cases;

}
