package com.aura.clinician.exception;

public class DiseaseModuleNotFoundException extends RuntimeException {
    public DiseaseModuleNotFoundException(String diseaseType) {
        super("No disease module registered for type: " + diseaseType);
    }
}
