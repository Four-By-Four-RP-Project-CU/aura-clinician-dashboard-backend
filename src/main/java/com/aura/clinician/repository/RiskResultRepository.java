package com.aura.clinician.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aura.clinician.domain.RiskResultDocument;

public interface RiskResultRepository extends MongoRepository<RiskResultDocument, String> {
    Optional<RiskResultDocument> findByCaseId(String caseId);
}
