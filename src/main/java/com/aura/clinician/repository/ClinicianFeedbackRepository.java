package com.aura.clinician.repository;

import com.aura.clinician.Entities.ClinicianFeedbackEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClinicianFeedbackRepository extends MongoRepository<ClinicianFeedbackEntity, String> {


    List<ClinicianFeedbackEntity> findByTrainedFalse();

    boolean existsByCaseId(String caseId);
}
