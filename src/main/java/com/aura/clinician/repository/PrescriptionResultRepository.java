package com.aura.clinician.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aura.clinician.domain.PrescriptionResultDocument;

public interface PrescriptionResultRepository extends MongoRepository<PrescriptionResultDocument, String> {
    Optional<PrescriptionResultDocument> findByCaseId(String caseId);
}
