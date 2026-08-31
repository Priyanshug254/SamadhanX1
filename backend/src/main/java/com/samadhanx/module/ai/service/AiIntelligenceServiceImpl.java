package com.samadhanx.module.ai.service;

import com.samadhanx.module.ai.dto.AiChallengeAnalysisRequest;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisResponse;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisRequest;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisResponse;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationRequest;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationResponse;
import com.samadhanx.module.ai.provider.GeminiAiProvider;
import com.samadhanx.module.ai.provider.RuleBasedAiFallbackProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiIntelligenceServiceImpl implements AiIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(AiIntelligenceServiceImpl.class);

    private final GeminiAiProvider geminiAiProvider;
    private final RuleBasedAiFallbackProvider ruleBasedAiFallbackProvider;

    @Value("${samadhanx.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${samadhanx.ai.provider:gemini}")
    private String configuredProvider;

    @Override
    public AiChallengeAnalysisResponse analyzeChallenge(AiChallengeAnalysisRequest request) {
        if (aiEnabled && "gemini".equalsIgnoreCase(configuredProvider) && geminiAiProvider.isConfigured()) {
            try {
                log.info("Dispatching challenge AI analysis to Google Gemini LLM...");
                return geminiAiProvider.analyzeChallenge(request);
            } catch (Exception e) {
                log.warn("Gemini AI challenge analysis failed ({}), gracefully falling back to deterministic engine.", e.getMessage());
            }
        }

        log.debug("Using Rule-Based Fallback Engine for challenge analysis.");
        return ruleBasedAiFallbackProvider.analyzeChallenge(request);
    }

    @Override
    public AiDuplicateAnalysisResponse checkDuplicates(AiDuplicateAnalysisRequest request) {
        if (aiEnabled && "gemini".equalsIgnoreCase(configuredProvider) && geminiAiProvider.isConfigured()) {
            try {
                log.info("Dispatching semantic duplicate detection to Google Gemini LLM...");
                return geminiAiProvider.checkDuplicates(request);
            } catch (Exception e) {
                log.warn("Gemini AI duplicate check failed ({}), falling back to deterministic lexical engine.", e.getMessage());
            }
        }

        log.debug("Using Rule-Based Fallback Engine for duplicate detection.");
        return ruleBasedAiFallbackProvider.checkDuplicates(request);
    }

    @Override
    public AiSolutionRecommendationResponse generateSolutionRecommendation(AiSolutionRecommendationRequest request) {
        if (aiEnabled && "gemini".equalsIgnoreCase(configuredProvider) && geminiAiProvider.isConfigured()) {
            try {
                log.info("Dispatching R&D solution recommendation to Google Gemini LLM...");
                return geminiAiProvider.generateSolutionRecommendation(request);
            } catch (Exception e) {
                log.warn("Gemini AI solution recommendation failed ({}), falling back to domain R&D blueprints.", e.getMessage());
            }
        }

        log.debug("Using Rule-Based Fallback Engine for solution recommendation.");
        return ruleBasedAiFallbackProvider.generateSolutionRecommendation(request);
    }
}
