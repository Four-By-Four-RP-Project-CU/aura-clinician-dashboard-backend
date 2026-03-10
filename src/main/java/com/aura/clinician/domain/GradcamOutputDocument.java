package com.aura.clinician.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "gradcam_outputs")
public class GradcamOutputDocument {
    @Id
    private String id;

    private String caseId;
    private String baseImageUrl;
    private String heatmapUrl;
}
