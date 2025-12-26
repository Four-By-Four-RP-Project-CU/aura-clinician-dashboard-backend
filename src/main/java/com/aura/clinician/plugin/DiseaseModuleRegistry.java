package com.aura.clinician.plugin;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aura.clinician.exception.DiseaseModuleNotFoundException;

@Component
public class DiseaseModuleRegistry {
    private final List<DiseaseModule> modules;

    public DiseaseModuleRegistry(List<DiseaseModule> modules) {
        this.modules = modules;
    }

    public DiseaseModule getModule(String diseaseType) {
        return modules.stream()
            .filter(module -> module.supports(diseaseType))
            .findFirst()
            .orElseThrow(() -> new DiseaseModuleNotFoundException(diseaseType));
    }
}
