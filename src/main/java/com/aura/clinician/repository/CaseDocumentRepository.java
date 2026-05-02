package com.aura.clinician.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aura.clinician.domain.CaseDocument;

public interface CaseDocumentRepository extends MongoRepository<CaseDocument, String> {
}
