package com.aura.clinician.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.aura.clinician.domain.ClinicalReviewDocument;

public interface ClinicalReviewRepository extends MongoRepository<ClinicalReviewDocument, String> {}
