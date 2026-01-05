package com.aura.clinician.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aura.clinician.domain.GuidelineRuleDocument;

public interface GuidelineRuleRepository extends MongoRepository<GuidelineRuleDocument, String> {
    List<GuidelineRuleDocument> findByDiseaseType(String diseaseType);
}
