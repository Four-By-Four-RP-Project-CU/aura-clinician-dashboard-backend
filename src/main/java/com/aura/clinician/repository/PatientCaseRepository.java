package com.aura.clinician.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aura.clinician.domain.PatientCaseDocument;

public interface PatientCaseRepository extends MongoRepository<PatientCaseDocument, String> {
    Optional<PatientCaseDocument> findByCaseId(String caseId);
}
