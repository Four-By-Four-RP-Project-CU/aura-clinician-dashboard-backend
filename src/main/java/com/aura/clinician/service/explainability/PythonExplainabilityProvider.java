package com.aura.clinician.service.explainability;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.aura.clinician.api.dto.GradCamArtifact;
import com.aura.clinician.api.dto.ShapContribution;
import com.aura.clinician.domain.PatientCaseDocument;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@Profile("python")
@RequiredArgsConstructor
public class PythonExplainabilityProvider implements ExplainabilityProvider {
    private static final Logger logger = LoggerFactory.getLogger(PythonExplainabilityProvider.class);
    private final RestTemplate restTemplate;
    private final Map<String, CacheEntry<List<ShapContribution>>> shapCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<GradCamArtifact>> gradcamCache = new ConcurrentHashMap<>();

    @Value("${explainability.service.url:http://localhost:8001}")
    private String baseUrl;

    @Value("${explainability.service.api-key:}")
    private String apiKey;

    @Value("${explainability.cache.ttl-seconds:900}")
    private long cacheTtlSeconds;

    @jakarta.annotation.PostConstruct
    void logActive() {
        logger.info("Explainability provider: PythonExplainabilityProvider (profile=python) baseUrl={}", baseUrl);
    }

    @Override
    public List<ShapContribution> getShap(String caseId, PatientCaseDocument patientCase) {
        if (patientCase == null) {
            return List.of();
        }
        CacheEntry<List<ShapContribution>> cachedShap = shapCache.get(caseId);
        if (cachedShap != null && !cachedShap.isExpired()) {
            logger.info("SHAP cache hit for caseId={}", caseId);
            return cachedShap.getValue();
        }
        try {
            logger.info("Calling SHAP service for caseId={}", caseId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("caseId", caseId);
            payload.put("features", buildFeatureMap(patientCase));

            ResponseEntity<ShapResponse> response = restTemplate.exchange(
                URI.create(baseUrl + "/explain/shap"),
                HttpMethod.POST,
                new HttpEntity<>(payload, buildHeaders()),
                ShapResponse.class
            );

            ShapResponse body = response.getBody();
            if (body == null || body.getShapScores() == null) {
                return List.of();
            }
            List<ShapContribution> contributions = new ArrayList<>();
            for (ShapScore score : body.getShapScores()) {
                ShapContribution item = new ShapContribution();
                item.setFeature(score.getFeature());
                item.setContribution(score.getContribution());
                item.setDirection(score.getContribution() >= 0 ? "POSITIVE" : "NEGATIVE");
                contributions.add(item);
            }
            shapCache.put(caseId, new CacheEntry<>(contributions, expiresAtMillis()));
            return contributions;
        } catch (Exception ex) {
            logger.warn("SHAP service unavailable for caseId={}", caseId);
            return List.of();
        }
    }

    @Override
    public GradCamArtifact getGradcam(String caseId, PatientCaseDocument patientCase) {
        GradCamArtifact artifact = new GradCamArtifact();
        if (patientCase != null) {
            artifact.setBaseImageUrl(patientCase.getImagePath());
        }
        String imagePathForKey = patientCase != null ? patientCase.getImagePath() : "";
        String gradcamCacheKey = caseId + "::" + (imagePathForKey != null ? imagePathForKey : "");
        CacheEntry<GradCamArtifact> cachedGradcam = gradcamCache.get(gradcamCacheKey);
        if (cachedGradcam != null && !cachedGradcam.isExpired()) {
            logger.info("Grad-CAM cache hit for caseId={}", caseId);
            return cachedGradcam.getValue();
        }
        try {
            logger.info("Calling Grad-CAM service for caseId={} imagePath={}", caseId,
                patientCase != null ? patientCase.getImagePath() : null);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("caseId", caseId);
            String imagePath = patientCase != null ? patientCase.getImagePath() : null;
            if (imagePath != null && imagePath.startsWith("http")) {
                payload.put("imageUrl", imagePath);
            } else if (imagePath != null) {
                payload.put("imagePath", imagePath);
            }

            ResponseEntity<GradcamResponse> response = restTemplate.exchange(
                URI.create(baseUrl + "/explain/gradcam"),
                HttpMethod.POST,
                new HttpEntity<>(payload, buildHeaders()),
                GradcamResponse.class
            );
            GradcamResponse body = response.getBody();
            if (body != null) {
                if (body.getBaseImageUrl() != null) {
                    artifact.setBaseImageUrl(body.getBaseImageUrl());
                }
                // Prefer overlay (heatmap blended onto original image) for display.
                // Fall back to raw heatmap if overlay is unavailable.
                String displayUrl = body.getOverlayUrl() != null ? body.getOverlayUrl() : body.getHeatmapUrl();
                if (displayUrl != null) {
                    artifact.setHeatmapUrl(displayUrl);
                }
                logger.info("Grad-CAM ready for caseId={} overlayUrl={} heatmapUrl={}", caseId, body.getOverlayUrl(), body.getHeatmapUrl());
            }
            gradcamCache.put(gradcamCacheKey, new CacheEntry<>(artifact, expiresAtMillis()));
        } catch (Exception ex) {
            logger.warn("Grad-CAM service unavailable for caseId={}", caseId);
        }
        return artifact;
    }

    private long expiresAtMillis() {
        long ttl = Math.max(30L, cacheTtlSeconds);
        return System.currentTimeMillis() + (ttl * 1000L);
    }

    private Map<String, Object> buildFeatureMap(PatientCaseDocument patientCase) {
        Map<String, Object> features = new LinkedHashMap<>();
        features.put("age", patientCase.getAgeYears());
        if (patientCase.getLabs() != null) {
            features.put("CRP", patientCase.getLabs().getCrp());
            features.put("IgE", patientCase.getLabs().getIgE());
            features.put("VitD", patientCase.getLabs().getVitD());
        }
        if (patientCase.getSymptoms() != null) {
            features.put("itchingScore", parseNumeric(patientCase.getSymptoms().getItchingScore()));
            features.put("angioedemaPresent", patientCase.getSymptoms().getAngioedemaPresent());
        }
        features.put("uctTotal", sumScores(patientCase.getUct()));
        features.put("aectTotal", sumScores(patientCase.getAect()));
        return features;
    }

    private Integer sumScores(PatientCaseDocument.QuestionnaireScore score) {
        if (score == null || score.getQ1() == null || score.getQ2() == null || score.getQ3() == null || score.getQ4() == null) {
            return null;
        }
        return score.getQ1() + score.getQ2() + score.getQ3() + score.getQ4();
    }

    private Double parseNumeric(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.trim().replaceAll("[^0-9.]", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("X-API-KEY", apiKey);
        }
        return headers;
    }

    @Data
    private static class ShapResponse {
        private String caseId;
        private boolean shapAvailable;
        private double baseValue;
        private List<ShapScore> shapScores;
        private String error;
    }

    @Data
    private static class ShapScore {
        private String feature;
        private double contribution;
    }

    @Data
    private static class GradcamResponse {
        private String caseId;
        private boolean gradCamAvailable;
        private String heatmapPath;
        private String heatmapUrl;
        private String overlayPath;
        private String overlayUrl;
        private String baseImagePath;
        private String baseImageUrl;
        private String error;
    }

    @Data
    private static class CacheEntry<T> {
        private final T value;
        private final long expiresAtMillis;

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }
}
