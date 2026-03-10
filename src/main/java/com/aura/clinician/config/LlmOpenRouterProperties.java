package com.aura.clinician.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "llm.openrouter")
public class LlmOpenRouterProperties {
    private String baseUrl = "https://openrouter.ai";
    private String chatPath = "/api/v1/chat/completions";
    private String model = "google/gemma-3-12b-it:free";
    private String apiKey = "";
    private int timeoutSeconds = 8;
    private double temperature = 0.1;
}
