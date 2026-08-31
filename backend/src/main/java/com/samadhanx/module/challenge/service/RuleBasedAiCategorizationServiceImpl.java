package com.samadhanx.module.challenge.service;

import com.samadhanx.module.ai.dto.AiChallengeAnalysisRequest;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisResponse;
import com.samadhanx.module.ai.service.AiIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI Categorization Engine for SamadhanX.
 * Integrates with the LLM Intelligence Layer (Gemini) while maintaining
 * a local deterministic domain taxonomy dictionary as a crash-proof fallback.
 */
@Service
@Primary
@RequiredArgsConstructor
public class RuleBasedAiCategorizationServiceImpl implements AiCategorizationService {

    private final AiIntelligenceService aiIntelligenceService;

    private static final Map<String, DomainDictionary> DOMAIN_DICTIONARIES = new HashMap<>();

    static {
        DOMAIN_DICTIONARIES.put("WATER_SANITATION", new DomainDictionary(
                "WATER_SANITATION",
                List.of("water", "drinking", "arsenic", "fluoride", "contamination", "hand pump", "borewell", "sewage", "drainage", "sanitation", "toilet", "purification", "pipeline", "salinity", "pond", "river", "well"),
                "Drinking Water & Sanitation"
        ));

        DOMAIN_DICTIONARIES.put("AGRI_TECH", new DomainDictionary(
                "AGRI_TECH",
                List.of("crop", "farm", "farmer", "agriculture", "soil", "irrigation", "pesticide", "fertilizer", "pest", "harvest", "seed", "drought", "post-harvest", "storage", "mandi", "yield"),
                "Agriculture & Farm Technology"
        ));

        DOMAIN_DICTIONARIES.put("CLEAN_ENERGY", new DomainDictionary(
                "CLEAN_ENERGY",
                List.of("solar", "electricity", "power", "grid", "biomass", "energy", "transformer", "voltage", "blackout", "load shedding", "renewable", "generator", "battery"),
                "Clean & Renewable Energy"
        ));

        DOMAIN_DICTIONARIES.put("HEALTHCARE", new DomainDictionary(
                "HEALTHCARE",
                List.of("health", "hospital", "clinic", "doctor", "medicine", "disease", "epidemic", "malaria", "dengue", "fever", "ambulance", "phc", "chc", "maternal", "nutrition", "vaccine"),
                "Healthcare & Public Hygiene"
        ));

        DOMAIN_DICTIONARIES.put("URBAN_MOBILITY", new DomainDictionary(
                "URBAN_MOBILITY",
                List.of("road", "pothole", "traffic", "street light", "bus", "transport", "footpath", "bridge", "highway", "congestion", "signal", "pavement"),
                "Urban Mobility & Roads"
        ));

        DOMAIN_DICTIONARIES.put("WASTE_MGMT", new DomainDictionary(
                "WASTE_MGMT",
                List.of("garbage", "waste", "trash", "plastic", "dump", "landfill", "recycling", "compost", "solid waste", "litter", "disposal"),
                "Solid & Plastic Waste Management"
        ));

        DOMAIN_DICTIONARIES.put("DISASTER_RESILIENCE", new DomainDictionary(
                "DISASTER_RESILIENCE",
                List.of("flood", "landslide", "cyclone", "storm", "earthquake", "inundation", "waterlogging", "embankment", "shelter", "rescue", "warning"),
                "Disaster Resilience & Early Warning"
        ));

        DOMAIN_DICTIONARIES.put("EDUCATION_SKILLING", new DomainDictionary(
                "EDUCATION_SKILLING",
                List.of("school", "student", "teacher", "education", "classroom", "books", "digital", "skilling", "vocational", "disabled", "literacy", "dropout"),
                "Education & Digital Skilling"
        ));
    }

    @Override
    public AiCategorizationResult categorize(String title, String description, BigDecimal latitude, BigDecimal longitude) {
        try {
            AiChallengeAnalysisResponse aiResponse = aiIntelligenceService.analyzeChallenge(
                    AiChallengeAnalysisRequest.builder()
                            .title(title)
                            .description(description)
                            .latitude(latitude)
                            .longitude(longitude)
                            .build()
            );

            if (aiResponse != null && aiResponse.getSuggestedDomain() != null) {
                return new AiCategorizationResult(
                        aiResponse.getSuggestedDomain(),
                        aiResponse.getConfidenceScore() != null ? aiResponse.getConfidenceScore() : BigDecimal.valueOf(0.90),
                        aiResponse.getKeywords() != null ? aiResponse.getKeywords() : List.of("infrastructure"),
                        aiResponse.getSuggestedSubCategory() != null ? aiResponse.getSuggestedSubCategory() : "General Societal Issue",
                        aiResponse.getReasoning() != null ? aiResponse.getReasoning() : "AI categorized based on context",
                        aiResponse.getModelProvider() != null ? aiResponse.getModelProvider() : "AI-Intelligence-Engine"
                );
            }
        } catch (Exception e) {
            // Fallback immediately to local dictionary
        }

        return fallbackLocalCategorize(title, description);
    }

    private AiCategorizationResult fallbackLocalCategorize(String title, String description) {
        String combined = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase(Locale.ROOT);

        String bestDomain = "WATER_SANITATION";
        int maxMatches = 0;
        List<String> matchedKeywords = new ArrayList<>();
        String suggestedSubCategory = "General Societal Issue";

        for (Map.Entry<String, DomainDictionary> entry : DOMAIN_DICTIONARIES.entrySet()) {
            DomainDictionary dict = entry.getValue();
            int currentMatches = 0;
            List<String> currentKeywords = new ArrayList<>();

            for (String kw : dict.keywords) {
                if (combined.contains(kw)) {
                    currentMatches++;
                    if (!currentKeywords.contains(kw)) {
                        currentKeywords.add(kw);
                    }
                }
            }

            if (currentMatches > maxMatches) {
                maxMatches = currentMatches;
                bestDomain = entry.getKey();
                matchedKeywords = currentKeywords;
                suggestedSubCategory = dict.defaultSubCategory;
            }
        }

        double rawConfidence = maxMatches == 0 ? 0.50 : Math.min(0.98, 0.65 + (maxMatches * 0.08));
        BigDecimal confidence = BigDecimal.valueOf(rawConfidence).setScale(3, RoundingMode.HALF_UP);

        return new AiCategorizationResult(
                bestDomain,
                confidence,
                matchedKeywords.isEmpty() ? List.of("community issue") : matchedKeywords,
                suggestedSubCategory,
                "Categorized via deterministic keyword taxonomy [" + String.join(", ", matchedKeywords) + "]",
                "RuleBasedFallbackEngine"
        );
    }

    private record DomainDictionary(String domainCode, List<String> keywords, String defaultSubCategory) {}
}
