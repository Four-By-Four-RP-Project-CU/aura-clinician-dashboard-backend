package com.aura.clinician.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.JustificationItem;
import com.aura.clinician.api.dto.RecommendationItem;
import com.aura.clinician.domain.GuidelineRuleDocument;
import com.aura.clinician.repository.GuidelineRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuidelineMapper {
    private final GuidelineRuleRepository guidelineRuleRepository;

    public List<RecommendationItem> map(
        String diseaseType,
        GuidelineContext context,
        List<JustificationItem> justifications
    ) {
        List<GuidelineRuleDocument> rules = guidelineRuleRepository.findByDiseaseType(diseaseType);
        List<RecommendationItem> recommendations = new ArrayList<>();

        for (GuidelineRuleDocument rule : rules) {
            if (!rule.isEnabled()) {
                continue;
            }
            if (!matchesCondition(rule.getCondition(), context)) {
                continue;
            }

            RecommendationItem item = new RecommendationItem();
            item.setType("Guideline");
            item.setText(rule.getMessage());
            item.setGuidelineTag(rule.getGuidelineTag());
            item.setSeverity(rule.getSeverity());
            recommendations.add(item);
        }

        if (justifications != null && justifications.stream().anyMatch(j -> "REVIEW".equalsIgnoreCase(j.getSeverity()))) {
            RecommendationItem item = new RecommendationItem();
            item.setType("Justification");
            item.setText("Specialist review recommended based on decision justification.");
            item.setGuidelineTag("EAACI");
            item.setSeverity("REVIEW");
            recommendations.add(item);
        }

        return recommendations;
    }

    private boolean matchesCondition(Map<String, Object> condition, GuidelineContext context) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            Object actual = context.resolve(entry.getKey());
            if (!evaluateCondition(actual, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Object actual, Object expected) {
        if (expected instanceof Map<?, ?> expectedMap) {
            Map<String, Object> map = (Map<String, Object>) expectedMap;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!compareOperator(actual, entry.getKey(), entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        if (expected == null) {
            return actual == null;
        }
        if (actual == null) {
            return false;
        }
        if (expected instanceof Number expectedNumber && actual instanceof Number actualNumber) {
            return Double.compare(actualNumber.doubleValue(), expectedNumber.doubleValue()) == 0;
        }
        return expected.toString().equalsIgnoreCase(actual.toString());
    }

    private boolean compareOperator(Object actual, String operator, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        String normalized = normalizeOperator(operator);
        if (actual instanceof Number actualNumber && expected instanceof Number expectedNumber) {
            double left = actualNumber.doubleValue();
            double right = expectedNumber.doubleValue();
            return switch (normalized) {
                case "lt" -> left < right;
                case "lte" -> left <= right;
                case "gt" -> left > right;
                case "gte" -> left >= right;
                case "eq" -> Double.compare(left, right) == 0;
                case "neq" -> Double.compare(left, right) != 0;
                default -> false;
            };
        }
        return switch (normalized) {
            case "eq" -> expected.toString().equalsIgnoreCase(actual.toString());
            case "neq" -> !expected.toString().equalsIgnoreCase(actual.toString());
            default -> false;
        };
    }

    private String normalizeOperator(String operator) {
        if (operator == null) {
            return "";
        }
        return operator.startsWith("$") ? operator.substring(1) : operator;
    }
}
