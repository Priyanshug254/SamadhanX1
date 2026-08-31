package com.samadhanx.module.challenge.service;

import java.math.BigDecimal;
import java.util.List;

public interface AiCategorizationService {

    record AiCategorizationResult(
            String predictedDomainCode,
            BigDecimal confidenceScore,
            List<String> extractedKeywords,
            String suggestedSubCategory,
            String reasoning,
            String modelProvider
    ) {
        public AiCategorizationResult(
                String predictedDomainCode,
                BigDecimal confidenceScore,
                List<String> extractedKeywords,
                String suggestedSubCategory
        ) {
            this(predictedDomainCode, confidenceScore, extractedKeywords, suggestedSubCategory, "Heuristic domain keyword taxonomy", "RuleBasedFallbackEngine");
        }
    }

    AiCategorizationResult categorize(String title, String description, BigDecimal latitude, BigDecimal longitude);
}
