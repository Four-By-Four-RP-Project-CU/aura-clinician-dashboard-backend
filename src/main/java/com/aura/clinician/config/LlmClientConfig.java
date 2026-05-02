package com.aura.clinician.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LlmClientConfig {

    @Bean(name = "openRouterWebClient")
    public WebClient openRouterWebClient(
        WebClient.Builder builder,
        LlmOpenRouterProperties properties
    ) {
        return builder
            .baseUrl(properties.getBaseUrl())
            .build();
    }
}
