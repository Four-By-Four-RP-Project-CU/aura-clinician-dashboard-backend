package com.aura.clinician.domain;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "shap_explanations")
public class ShapExplanationDocument {
    @Id
    private String id;

    private String caseId;
    private List<ShapFeature> features;

    @Data
    @NoArgsConstructor
    public static class ShapFeature {
        private String feature;
        private Double contribution;
        private String direction;
    }
}
