package com.samadhanx.module.challenge.service;

import com.samadhanx.module.ai.provider.RuleBasedAiFallbackProvider;
import com.samadhanx.module.ai.service.AiIntelligenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiCategorizationService Unit Tests")
class AiCategorizationServiceTest {

    private AiCategorizationService service;

    @BeforeEach
    void setUp() {
        RuleBasedAiFallbackProvider fallback = new RuleBasedAiFallbackProvider();
        AiIntelligenceServiceImpl aiIntelligenceService = new AiIntelligenceServiceImpl(null, fallback);
        ReflectionTestUtils.setField(aiIntelligenceService, "aiEnabled", true);
        ReflectionTestUtils.setField(aiIntelligenceService, "configuredProvider", "rule_based");
        service = new RuleBasedAiCategorizationServiceImpl(aiIntelligenceService);
    }

    @Test
    @DisplayName("Should accurately categorize water contamination issue into WATER_SANITATION domain")
    void shouldCategorizeWaterContamination() {
        String title = "Severe ground water arsenic contamination in Chandauli village hand pumps";
        String description = "Villagers are falling sick after drinking water from 4 deep borewells. Need filtration technology.";

        AiCategorizationService.AiCategorizationResult result = service.categorize(
                title, description, BigDecimal.valueOf(25.26), BigDecimal.valueOf(83.26)
        );

        assertNotNull(result);
        assertEquals("WATER_SANITATION", result.predictedDomainCode());
        assertTrue(result.confidenceScore().doubleValue() >= 0.70);
        assertTrue(result.extractedKeywords().contains("water") || result.extractedKeywords().contains("arsenic"));
    }

    @Test
    @DisplayName("Should accurately categorize crop disease issue into AGRI_TECH domain")
    void shouldCategorizeCropDisease() {
        String title = "Pest attack and soil degradation destroying mustard crop yield";
        String description = "Farmers in the district are facing unprecedented insect infestation and failed irrigation schedule.";

        AiCategorizationService.AiCategorizationResult result = service.categorize(
                title, description, BigDecimal.valueOf(26.84), BigDecimal.valueOf(80.94)
        );

        assertNotNull(result);
        assertEquals("AGRI_TECH", result.predictedDomainCode());
        assertTrue(result.confidenceScore().doubleValue() >= 0.70);
    }
}
