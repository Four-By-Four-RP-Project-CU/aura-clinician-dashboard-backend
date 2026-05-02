package com.aura.clinician.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aura.clinician.domain.AiPredictionDocument;

public interface AiPredictionRepository extends MongoRepository<AiPredictionDocument, String> {
    Optional<AiPredictionDocument> findByCaseId(String caseId);
}
