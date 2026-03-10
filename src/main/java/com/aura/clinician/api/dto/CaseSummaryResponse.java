package com.aura.clinician.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CaseSummaryResponse {
    private String caseId;
    private int age;
    private String gender;
}
