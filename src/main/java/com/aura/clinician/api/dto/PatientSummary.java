package com.aura.clinician.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PatientSummary {
    private String patientId;
    private String caseId;
    private Integer age;
    private String gender;
    private String hospital;
    private String visitDate;
    private String shape;
}
