package com.aura.clinician.repository;

import com.aura.clinician.Entities.ModelRegistryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ModelRegistryRepository extends MongoRepository<ModelRegistryEntity, String> {
    Optional<ModelRegistryEntity> findByModelVersion(String modelVersion);
    boolean existsByModelVersion(String modelVersion);
}
