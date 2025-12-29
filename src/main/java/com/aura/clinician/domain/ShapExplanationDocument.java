package com.aura.clinician.domain;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shap_explanations")
public class ShapExplanationDocument {
    @Id
    private String id;

    private String caseId;
    private List<ShapFeature> features;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public List<ShapFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<ShapFeature> features) {
        this.features = features;
    }

    public static class ShapFeature {
        private String feature;
        private Double contribution;
        private String direction;

        public String getFeature() {
            return feature;
        }

        public void setFeature(String feature) {
            this.feature = feature;
        }

        public Double getContribution() {
            return contribution;
        }

        public void setContribution(Double contribution) {
            this.contribution = contribution;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }
}
