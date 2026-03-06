package com.aura.clinician.service.llm;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.aura.clinician.api.dto.LlmEvidencePayload;
import com.aura.clinician.api.dto.LlmExplainabilityNarrative;
import com.aura.clinician.config.LlmOpenRouterProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OpenRouterLlmExplainabilityService implements LlmExplainabilityService {
    private static final Logger logger = LoggerFactory.getLogger(OpenRouterLlmExplainabilityService.class);
    private static final String SYSTEM_PROMPT =
        "You are a clinical explainability narrator for a clinician dashboard. " +
        "Your task is to interpret model evidence in clinician-facing language, not to produce a raw data summary. " +
        "Use only the provided evidence. Do not invent diagnoses, medications, tests, thresholds, dosage instructions, or new medical advice. " +
        "If any required evidence is missing, write exactly 'Insufficient data' for that field. " +
        "The explanation must clearly cover: what the model concluded, why it concluded that, which tabular features contributed most, whether image evidence supports the conclusion, and how UCT/AECT influence interpretation. " +
        "Use cautious wording such as: suggests, appears consistent with, was influenced by, supports, may indicate, closer review may be warranted. " +
        "Return ONLY valid JSON with exactly these keys: summary, decisionRationale, tabularEvidence, imageEvidence, controlStatus, recommendations, safetyNote. " +
        "Do not use markdown, code fences, bullet markdown, or backticks. " +
        "Do not repeat the full evidence JSON or internal field names. " +
        "Do not place nested JSON objects in any text field. " +
        "summary must be 2-4 sentences. " +
        "decisionRationale must explicitly explain decision, reason, major contributors, image support, and UCT/AECT impact. " +
        "tabularEvidence, imageEvidence, and controlStatus must be plain text strings. " +
        "recommendations must be a short array of concise clinician-friendly considerations. " +
        "safetyNote must be one short sentence.";
    private static final Pattern DOSAGE_PATTERN = Pattern.compile(
        "(?i)\\b\\d+(?:\\.\\d+)?\\s*(mg|mcg|g|ml|iu|units?)\\b|\\b(bid|tid|qd|daily|twice\\s+daily|once\\s+daily)\\b"
    );
    private static final Set<String> MEDICATION_TERMS = Set.of(
        "omalizumab",
        "antihistamine",
        "cetirizine",
        "loratadine",
        "fexofenadine",
        "bilastine",
        "levocetirizine",
        "montelukast",
        "prednisone",
        "steroid",
        "cyclosporine",
        "dupilumab"
    );

    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final LlmOpenRouterProperties properties;
    private final Map<String, CacheEntry> narrativeCache = new ConcurrentHashMap<>();
    private static final Duration CACHE_TTL = Duration.ofMinutes(3);

    public OpenRouterLlmExplainabilityService(
        ObjectMapper objectMapper,
        @Qualifier("openRouterWebClient") WebClient webClient,
        LlmOpenRouterProperties properties
    ) {
        this.objectMapper = objectMapper;
        this.webClient = webClient;
        this.properties = properties;
        String apiKey = properties.getApiKey();
        logger.info("LLM API key configured: {}", apiKey != null && !apiKey.isBlank());
    }

    @Override
    public LlmExplainabilityNarrative generateNarrative(LlmEvidencePayload payload) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return fallback(payload, "LLM summary unavailable (API key not configured).");
        }

        try {
            String promptEvidence = objectMapper.writeValueAsString(payload);
            String cacheKey = buildCacheKey(promptEvidence);
            LlmExplainabilityNarrative cached = getFromCache(cacheKey);
            if (cached != null) {
                return cached;
            }

            JsonNode root = callOpenRouter(promptEvidence, true);

            if (root == null) {
                return fallback(payload, "LLM summary unavailable.");
            }

            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.isNull() || contentNode.asText().isBlank()) {
                return fallback(payload, "LLM summary unavailable.");
            }

            String content = cleanRawModelOutput(contentNode.asText());
            try {
                LlmExplainabilityNarrative narrative = objectMapper.readValue(content, LlmExplainabilityNarrative.class);
                LlmExplainabilityNarrative safeNarrative = sanitizeNarrative(narrative, payload);
                putInCache(cacheKey, safeNarrative);
                return safeNarrative;
            } catch (Exception parseEx) {
                LlmExplainabilityNarrative narrative = fallback(payload, "LLM summary available in plain text only.");
                narrative.setSummary(content);
                LlmExplainabilityNarrative safeNarrative = sanitizeNarrative(narrative, payload);
                putInCache(cacheKey, safeNarrative);
                return safeNarrative;
            }
        } catch (Exception ex) {
            String errorBody = extractErrorBody(ex);

            if (isTimeout(ex)) {
                logger.warn("LLM narrative generation timed out after {} seconds", properties.getTimeoutSeconds());
                return fallback(payload, "LLM summary unavailable (timeout).");
            }

            if (isDeveloperInstructionUnsupported(ex)) {
                logger.warn("OpenRouter provider rejected system role; retrying with single user message");
                try {
                    String promptEvidence = objectMapper.writeValueAsString(payload);
                    JsonNode retryRoot = callOpenRouter(promptEvidence, false);
                    return parseAndSanitize(payload, promptEvidence, retryRoot);
                } catch (Exception retryEx) {
                    logger.warn(
                        "LLM retry without system role failed: {} body={}",
                        retryEx.getMessage(),
                        extractErrorBody(retryEx)
                    );
                    return fallback(payload, "LLM summary unavailable.");
                }
            }

            if (isRateLimitError(ex)) {
                logger.warn("LLM 429 received from OpenRouter. body={}", errorBody);
                return fallback(payload, "LLM summary unavailable (rate-limited).");
            }

            logger.warn("LLM narrative generation failed via OpenRouter: {} body={}", ex.getMessage(), errorBody);
            return fallback(payload, "LLM summary unavailable.");
        }
    }

    private JsonNode callOpenRouter(String promptEvidence, boolean useSystemRole) {
        Map<String, Object> requestBody = buildRequestBody(promptEvidence, useSystemRole);
        return webClient.post()
            .uri(properties.getChatPath())
            .header("Authorization", "Bearer " + properties.getApiKey())
            .header("HTTP-Referer", "http://localhost")
            .header("X-OpenRouter-Title", "AURA Clinician Dashboard")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
            .block();
    }

    private LlmExplainabilityNarrative parseAndSanitize(
        LlmEvidencePayload payload,
        String promptEvidence,
        JsonNode root
    ) {
        if (root == null) {
            return fallback(payload, "LLM summary unavailable.");
        }

        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.isNull() || contentNode.asText().isBlank()) {
            return fallback(payload, "LLM summary unavailable.");
        }

        String cacheKey = buildCacheKey(promptEvidence);
        String content = cleanRawModelOutput(contentNode.asText());
        try {
            LlmExplainabilityNarrative narrative = objectMapper.readValue(content, LlmExplainabilityNarrative.class);
            LlmExplainabilityNarrative safeNarrative = sanitizeNarrative(narrative, payload);
            putInCache(cacheKey, safeNarrative);
            return safeNarrative;
        } catch (Exception parseEx) {
            LlmExplainabilityNarrative narrative = fallback(payload, "LLM summary available in plain text only.");
            narrative.setSummary(content);
            LlmExplainabilityNarrative safeNarrative = sanitizeNarrative(narrative, payload);
            putInCache(cacheKey, safeNarrative);
            return safeNarrative;
        }
    }

    private LlmExplainabilityNarrative fallback(LlmEvidencePayload payload, String summary) {
        LlmExplainabilityNarrative narrative = new LlmExplainabilityNarrative();
        narrative.setSummary(summary);
        narrative.setDecisionRationale("Insufficient data");
        narrative.setTabularEvidence(
            "Subtype=" + safe(payload.getSubtypePredictionLabel()) +
            ", confidence=" + safe(payload.getConfidence()) +
            ", UCT=" + safe(payload.getUctScore()) + " (" + safe(payload.getUctStatus()) + ")" +
            ", AECT=" + safe(payload.getAectScore()) + " (" + safe(payload.getAectStatus()) + ")"
        );
        narrative.setImageEvidence(payload.getGradCamSummary());
        narrative.setControlStatus(
            "UCT status: " + safe(payload.getUctStatus()) + ", AECT status: " + safe(payload.getAectStatus())
        );
        narrative.setRecommendations(new ArrayList<>(payload.getRecommendations()));
        narrative.setSafetyNote(
            "Advisory summary only. Use structured evidence and clinician judgment. " +
            "No diagnosis or medication/dosage instructions are provided."
        );
        return narrative;
    }

    private LlmExplainabilityNarrative sanitizeNarrative(LlmExplainabilityNarrative narrative, LlmEvidencePayload payload) {
        if (narrative == null) {
            return fallback(payload, "LLM summary unavailable.");
        }
        normalizeNarrativeFields(narrative);

        List<String> allowedRecommendations = payload != null && payload.getRecommendations() != null
            ? payload.getRecommendations().stream()
                .filter(text -> text != null && !text.isBlank())
                .toList()
            : Collections.emptyList();
        Set<String> allowedRecommendationSet = allowedRecommendations.stream()
            .map(this::normalizeText)
            .collect(Collectors.toSet());

        List<String> safeRecommendations = new ArrayList<>();
        if (narrative.getRecommendations() != null) {
            for (String item : narrative.getRecommendations()) {
                if (item == null || item.isBlank()) {
                    continue;
                }
                if (allowedRecommendationSet.contains(normalizeText(item))) {
                    safeRecommendations.add(item);
                } else {
                    logger.warn("LLM output contained recommendation outside allowed list; dropping item");
                }
            }
        }
        if (safeRecommendations.isEmpty() && !allowedRecommendations.isEmpty()) {
            safeRecommendations = new ArrayList<>(allowedRecommendations);
        }
        narrative.setRecommendations(
            safeRecommendations.stream()
                .map(this::cleanPlainTextField)
                .filter(text -> text != null && !text.isBlank())
                .limit(4)
                .toList()
        );

        if (containsDosageInstruction(narrative) || containsDisallowedMedication(narrative, payload)) {
            logger.warn("LLM output rejected due to medication/dosage policy violation; using fallback narrative");
            return fallback(payload, "LLM summary unavailable (safety filter).");
        }

        Set<String> missingData = payload != null && payload.getMissingData() != null
            ? new HashSet<>(payload.getMissingData())
            : Collections.emptySet();
        if (missingData.contains("shapContributions")) {
            narrative.setTabularEvidence("Insufficient data");
        }
        if (missingData.contains("gradCamHeatmap")) {
            narrative.setImageEvidence("Insufficient data");
        }

        if (narrative.getSummary() == null || narrative.getSummary().isBlank()) {
            narrative.setSummary("LLM summary unavailable.");
        }
        if (narrative.getDecisionRationale() == null || narrative.getDecisionRationale().isBlank()) {
            narrative.setDecisionRationale("Insufficient data");
        }
        if (narrative.getControlStatus() == null || narrative.getControlStatus().isBlank()) {
            narrative.setControlStatus("Insufficient data");
        }
        if (narrative.getSafetyNote() == null || narrative.getSafetyNote().isBlank()) {
            narrative.setSafetyNote(
                "Advisory summary only. Use structured evidence and clinician judgment. " +
                "No diagnosis or medication/dosage instructions are provided."
            );
        }
        normalizeNarrativeFields(narrative);
        return narrative;
    }

    private boolean containsDosageInstruction(LlmExplainabilityNarrative narrative) {
        String text = flattenNarrativeText(narrative);
        return DOSAGE_PATTERN.matcher(text).find();
    }

    private boolean containsDisallowedMedication(LlmExplainabilityNarrative narrative, LlmEvidencePayload payload) {
        Set<String> allowedMedicationTerms = extractAllowedMedicationTerms(payload);
        String text = flattenNarrativeText(narrative).toLowerCase();
        for (String medication : MEDICATION_TERMS) {
            if (text.contains(medication) && !allowedMedicationTerms.contains(medication)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> extractAllowedMedicationTerms(LlmEvidencePayload payload) {
        if (payload == null) {
            return Collections.emptySet();
        }
        Set<String> allowed = new HashSet<>();
        List<String> sourceTexts = new ArrayList<>();
        if (payload.getRecommendations() != null) {
            sourceTexts.addAll(payload.getRecommendations());
        }
        if (payload.getTreatmentPathwayList() != null) {
            sourceTexts.addAll(payload.getTreatmentPathwayList());
        }
        String text = String.join(" ", sourceTexts).toLowerCase();
        for (String medication : MEDICATION_TERMS) {
            if (text.contains(medication)) {
                allowed.add(medication);
            }
        }
        return allowed;
    }

    private String flattenNarrativeText(LlmExplainabilityNarrative narrative) {
        String recommendationsText = narrative.getRecommendations() == null
            ? ""
            : String.join(" ", narrative.getRecommendations());
        return String.join(
            " ",
            safe(narrative.getSummary()),
            safe(narrative.getDecisionRationale()),
            safe(narrative.getTabularEvidence()),
            safe(narrative.getImageEvidence()),
            safe(narrative.getControlStatus()),
            recommendationsText,
            safe(narrative.getSafetyNote())
        );
    }

    private boolean isTimeout(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof TimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isRateLimitError(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof WebClientResponseException responseException
                && responseException.getStatusCode().value() == 429) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isDeveloperInstructionUnsupported(Throwable ex) {
        String body = extractErrorBody(ex);
        if (body == null) {
            return false;
        }
        return body.toLowerCase().contains("developer instruction is not enabled");
    }

    private String buildCacheKey(String promptEvidence) {
        return Integer.toHexString(promptEvidence.hashCode());
    }

    private LlmExplainabilityNarrative getFromCache(String cacheKey) {
        CacheEntry entry = narrativeCache.get(cacheKey);
        if (entry == null) {
            return null;
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            narrativeCache.remove(cacheKey);
            return null;
        }
        return entry.narrative();
    }

    private void putInCache(String cacheKey, LlmExplainabilityNarrative narrative) {
        if (narrative == null) {
            return;
        }
        narrativeCache.put(cacheKey, new CacheEntry(narrative, Instant.now().plus(CACHE_TTL)));
    }

    private Map<String, Object> buildRequestBody(String evidenceJson, boolean useSystemRole) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("temperature", Math.min(properties.getTemperature(), 0.1d));
        requestBody.put("response_format", Map.of("type", "json_object"));
        if (useSystemRole) {
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", "Evidence JSON: " + evidenceJson)
            ));
        } else {
            requestBody.put("messages", List.of(
                Map.of(
                    "role",
                    "user",
                    "content",
                    "Instruction: " + SYSTEM_PROMPT + "\n\nEvidence JSON: " + evidenceJson
                )
            ));
        }
        return requestBody;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String cleanRawModelOutput(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
            .replace("```json", "")
            .replace("```JSON", "")
            .replace("```", "")
            .trim();
    }

    private void normalizeNarrativeFields(LlmExplainabilityNarrative narrative) {
        narrative.setSummary(cleanPlainTextField(narrative.getSummary()));
        narrative.setDecisionRationale(cleanPlainTextField(narrative.getDecisionRationale()));
        narrative.setTabularEvidence(cleanPlainTextField(narrative.getTabularEvidence()));
        narrative.setImageEvidence(cleanPlainTextField(narrative.getImageEvidence()));
        narrative.setControlStatus(cleanPlainTextField(narrative.getControlStatus()));
        narrative.setSafetyNote(cleanPlainTextField(narrative.getSafetyNote()));
    }

    private String cleanPlainTextField(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value
            .replace("```json", " ")
            .replace("```JSON", " ")
            .replace("```", " ")
            .replaceAll("(?i)^\\s*json\\s*[:\\-]?\\s*", "")
            .replaceAll("\\s+", " ")
            .trim();
        if (cleaned.startsWith("{") || cleaned.startsWith("[")) {
            try {
                JsonNode node = objectMapper.readTree(cleaned);
                String extracted = extractPlainTextFromNode(node);
                if (extracted != null && !extracted.isBlank()) {
                    return extracted.replaceAll("\\s+", " ").trim();
                }
                return "Insufficient data";
            } catch (Exception ignored) {
                return "Insufficient data";
            }
        }
        return cleaned;
    }

    private String extractPlainTextFromNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        if (node.isObject()) {
            String[] preferredKeys = {"summary", "decisionRationale", "tabularEvidence", "imageEvidence", "controlStatus", "safetyNote", "text", "message", "content"};
            for (String key : preferredKeys) {
                JsonNode child = node.get(key);
                String text = extractPlainTextFromNode(child);
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                String text = extractPlainTextFromNode(fields.next().getValue());
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
            return null;
        }
        if (node.isArray()) {
            List<String> items = new ArrayList<>();
            for (JsonNode child : node) {
                String text = extractPlainTextFromNode(child);
                if (text != null && !text.isBlank()) {
                    items.add(text);
                }
            }
            if (items.isEmpty()) {
                return null;
            }
            return String.join("; ", items);
        }
        return node.asText(null);
    }

    private String safe(Object value) {
        return value == null ? "N/A" : value.toString();
    }

    private record CacheEntry(
        LlmExplainabilityNarrative narrative,
        Instant expiresAt
    ) {}

    private String extractErrorBody(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof WebClientResponseException responseException) {
                return responseException.getResponseBodyAsString();
            }
            current = current.getCause();
        }
        return null;
    }

}
