package com.aura.clinician.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aura.clinician.domain.ShapExplanationDocument;

public interface ShapExplanationRepository extends MongoRepository<ShapExplanationDocument, String> {
    Optional<ShapExplanationDocument> findByCaseId(String caseId);
}
