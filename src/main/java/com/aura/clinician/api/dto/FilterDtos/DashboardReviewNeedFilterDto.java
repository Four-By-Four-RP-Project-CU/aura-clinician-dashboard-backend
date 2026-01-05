package com.aura.clinician.api.dto.FilterDtos;

import com.aura.clinician.Enums.UncertaintyLevelEnum;
import lombok.Data;

@Data
public class DashboardReviewNeedFilterDto {

    private UncertaintyLevelEnum uncertaintyLevel;
    private boolean lowestConfidenceFirst;
    private boolean latestFirst;
}
