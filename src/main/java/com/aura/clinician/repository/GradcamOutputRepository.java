package com.aura.clinician.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aura.clinician.domain.GradcamOutputDocument;

public interface GradcamOutputRepository extends MongoRepository<GradcamOutputDocument, String> {
    Optional<GradcamOutputDocument> findByCaseId(String caseId);
}
