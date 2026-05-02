package com.aura.clinician.repository;

import com.aura.clinician.Entities.ClinicianFeedbackEntity;
import com.aura.clinician.Enums.ClinicianFinalDecision;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClinicianFeedbackRepository extends MongoRepository<ClinicianFeedbackEntity, String> {


    List<ClinicianFeedbackEntity> findByTrainedFalse();

    boolean existsByCaseId(String caseId);

    Optional<ClinicianFeedbackEntity> findTopByOrderByUpdatedAtDesc();

    long countByTrainedTrue();

    long countByClinicianFinalDecision(ClinicianFinalDecision decision);

    long countByClinicianFinalDecisionIn(Collection<ClinicianFinalDecision> decisions);
}
