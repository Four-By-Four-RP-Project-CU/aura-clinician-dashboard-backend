package com.aura.clinician.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

import com.aura.clinician.domain.CaseInputDocument;

public interface CaseInputRepository extends MongoRepository<CaseInputDocument, String> {
    Optional<CaseInputDocument> findByCaseId(String caseId);
}
