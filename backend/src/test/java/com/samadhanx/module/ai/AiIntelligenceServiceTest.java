package com.samadhanx.module.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisRequest;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisResponse;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisRequest;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisResponse;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationRequest;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationResponse;
import com.samadhanx.module.ai.provider.GeminiAiProvider;
import com.samadhanx.module.ai.provider.RuleBasedAiFallbackProvider;
import com.samadhanx.module.ai.service.AiIntelligenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiIntelligenceServiceTest {

    @Mock
    private GeminiAiProvider geminiAiProvider;

    private RuleBasedAiFallbackProvider fallbackProvider;
    private AiIntelligenceServiceImpl aiService;

    @BeforeEach
    void setUp() {
        fallbackProvider = new RuleBasedAiFallbackProvider();
        aiService = new AiIntelligenceServiceImpl(geminiAiProvider, fallbackProvider);
        ReflectionTestUtils.setField(aiService, "aiEnabled", true);
        ReflectionTestUtils.setField(aiService, "configuredProvider", "gemini");
    }

    @Test
    @DisplayName("Should use RuleBasedFallbackProvider when Gemini is unconfigured or fails")
    void testFallbackWhenGeminiFails() {
        when(geminiAiProvider.isConfigured()).thenReturn(true);
        when(geminiAiProvider.analyzeChallenge(any())).thenThrow(new RuntimeException("Gemini network timeout"));

        AiChallengeAnalysisRequest request = AiChallengeAnalysisRequest.builder()
                .title("High fluoride contamination in village drinking water handpump")
                .description("Over 50 children suffering from dental and skeletal fluorosis in Chandauli.")
                .district("Chandauli")
                .state("Uttar Pradesh")
                .build();

        AiChallengeAnalysisResponse response = aiService.analyzeChallenge(request);

        assertThat(response).isNotNull();
        assertThat(response.getSuggestedDomain()).isEqualTo("WATER_SANITATION");
        assertThat(response.isFallbackUsed()).isTrue();
        assertThat(response.getConfidenceScore()).isNotNull();
        assertThat(response.getPriorityScore()).isGreaterThan(BigDecimal.ZERO);
        assertThat(response.getKeywords()).contains("water", "fluoride");
    }

    @Test
    @DisplayName("Should parse and return Gemini LLM response when successful")
    void testGeminiSuccess() {
        when(geminiAiProvider.isConfigured()).thenReturn(true);

        AiChallengeAnalysisResponse mockGemini = AiChallengeAnalysisResponse.builder()
                .normalizedProblemStatement("Severe groundwater fluoride toxicity affecting child health")
                .suggestedDomain("WATER_SANITATION")
                .suggestedSubCategory("POTABLE_WATER_QUALITY")
                .severityAssessment("CRITICAL")
                .urgencyAssessment("HIGH")
                .affectedPopulationAssessment(2500)
                .confidenceScore(new BigDecimal("0.96"))
                .reasoning("Fluorosis symptoms and water source contamination strongly indicate water domain")
                .keywords(List.of("groundwater", "fluoride", "fluorosis"))
                .priorityScore(new BigDecimal("92.50"))
                .modelProvider("GoogleGemini (gemini-1.5-flash)")
                .fallbackUsed(false)
                .build();

        when(geminiAiProvider.analyzeChallenge(any())).thenReturn(mockGemini);

        AiChallengeAnalysisRequest request = AiChallengeAnalysisRequest.builder()
                .title("Severe groundwater fluoride toxicity")
                .description("Children showing skeletal fluorosis")
                .build();

        AiChallengeAnalysisResponse response = aiService.analyzeChallenge(request);

        assertThat(response).isNotNull();
        assertThat(response.getSuggestedDomain()).isEqualTo("WATER_SANITATION");
        assertThat(response.isFallbackUsed()).isFalse();
        assertThat(response.getModelProvider()).contains("GoogleGemini");
        assertThat(response.getPriorityScore()).isEqualTo(new BigDecimal("92.50"));
    }

    @Test
    @DisplayName("Should detect potential duplicates using fallback semantic similarity")
    void testDuplicateDetectionFallback() {
        UUID existingId = UUID.randomUUID();
        AiDuplicateAnalysisRequest.ExistingChallengeSnippet existing = AiDuplicateAnalysisRequest.ExistingChallengeSnippet.builder()
                .id(existingId)
                .trackingNumber("SMX-2026-0001")
                .title("Broken water pipeline causing drinking water shortage")
                .description("Main municipal pipeline ruptured near town square")
                .build();

        AiDuplicateAnalysisRequest request = AiDuplicateAnalysisRequest.builder()
                .candidateTitle("Drinking water pipeline broken and leaking")
                .candidateDescription("Ruptured pipeline near municipal square")
                .existingChallenges(List.of(existing))
                .build();

        // Gemini unconfigured -> fallback provider
        when(geminiAiProvider.isConfigured()).thenReturn(false);

        AiDuplicateAnalysisResponse dupResponse = aiService.checkDuplicates(request);

        assertThat(dupResponse).isNotNull();
        assertThat(dupResponse.isPotentialDuplicate()).isTrue();
        assertThat(dupResponse.getSimilarityScore()).isGreaterThan(new BigDecimal("0.30"));
        assertThat(dupResponse.getMatchedParentChallengeId()).isEqualTo(existingId);
    }

    @Test
    @DisplayName("Should generate solution recommendation for innovation challenges")
    void testSolutionRecommendationFallback() {
        when(geminiAiProvider.isConfigured()).thenReturn(false);

        AiSolutionRecommendationRequest request = AiSolutionRecommendationRequest.builder()
                .challengeId(UUID.randomUUID())
                .trackingNumber("SMX-2026-0089")
                .title("Arsenic contamination in shallow aquifer drinking water")
                .description("Chemical-free low-cost filtration required")
                .domainCode("WATER_SANITATION")
                .domainName("Water & Sanitation")
                .escalationReason("Standard municipal filters are ineffective for heavy metal removal")
                .district("Varanasi")
                .state("Uttar Pradesh")
                .build();

        AiSolutionRecommendationResponse solResponse = aiService.generateSolutionRecommendation(request);

        assertThat(solResponse).isNotNull();
        assertThat(solResponse.getProposedSolutionApproaches()).isNotEmpty();
        assertThat(solResponse.getRequiredTechnologies()).isNotEmpty();
        assertThat(solResponse.getSuggestedDisciplines()).contains("Materials Science & Nanotechnology");
        assertThat(solResponse.getSuggestedTRLStartingPoint()).isEqualTo(3);
    }
}
