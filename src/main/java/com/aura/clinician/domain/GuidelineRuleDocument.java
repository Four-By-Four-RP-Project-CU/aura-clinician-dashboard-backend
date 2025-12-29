package com.aura.clinician.domain;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "guideline_rules")
public class GuidelineRuleDocument {
    @Id
    private String id;

    private String diseaseType;
    private String ruleCode;
    private Map<String, Object> condition;
    private String message;
    private String guidelineTag;
    private String severity;
    private boolean enabled;
}
