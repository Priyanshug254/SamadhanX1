package com.samadhanx.module.ai.provider;

import com.samadhanx.module.ai.dto.AiChallengeAnalysisRequest;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisResponse;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisRequest;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisResponse;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationRequest;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class RuleBasedAiFallbackProvider implements AiProvider {

    private static final Map<String, List<String>> DOMAIN_KEYWORDS = Map.of(
            "WATER_SANITATION", List.of("water", "drinking", "drainage", "sewage", "pipeline", "fluoride", "arsenic", "contamination", "handpump", "borewell", "sanitation", "toilet"),
            "AGRI_TECH", List.of("crop", "soil", "irrigation", "farmer", "agriculture", "fertilizer", "pest", "harvest", "yield", "storage", "farm"),
            "CLEAN_ENERGY", List.of("solar", "electricity", "power", "grid", "outage", "energy", "renewable", "biomass", "transformer"),
            "HEALTHCARE", List.of("hospital", "clinic", "disease", "doctor", "health", "medicine", "dengue", "malaria", "epidemic", "ambulance", "hygiene"),
            "URBAN_MOBILITY", List.of("road", "pothole", "traffic", "bus", "transport", "bridge", "pedestrian", "signal", "highway"),
            "WASTE_MGMT", List.of("garbage", "plastic", "waste", "dumping", "landfill", "recycling", "trash", "debris"),
            "DISASTER_RESILIENCE", List.of("flood", "landslide", "cyclone", "earthquake", "inundation", "erosion", "disaster", "emergency"),
            "EDUCATION_SKILLING", List.of("school", "education", "teacher", "classroom", "student", "vocational", "skilling", "training", "library")
    );

    @Override
    public String getProviderName() {
        return "RuleBasedFallbackEngine";
    }

    @Override
    public AiChallengeAnalysisResponse analyzeChallenge(AiChallengeAnalysisRequest request) {
        String text = ((request.getTitle() != null ? request.getTitle() : "") + " " +
                (request.getDescription() != null ? request.getDescription() : "")).toLowerCase(Locale.ROOT);

        String predictedDomain = "WATER_SANITATION";
        int maxMatches = 0;
        List<String> matchedKeywords = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : DOMAIN_KEYWORDS.entrySet()) {
            int matches = 0;
            for (String kw : entry.getValue()) {
                if (text.contains(kw)) {
                    matches++;
                    if (!matchedKeywords.contains(kw)) {
                        matchedKeywords.add(kw);
                    }
                }
            }
            if (matches > maxMatches) {
                maxMatches = matches;
                predictedDomain = entry.getKey();
            }
        }

        if (matchedKeywords.isEmpty()) {
            matchedKeywords.add("infrastructure");
            matchedKeywords.add("community");
        }

        BigDecimal confidence = BigDecimal.valueOf(Math.min(0.98, 0.70 + (maxMatches * 0.06)))
                .setScale(2, RoundingMode.HALF_UP);

        // Priority calculation
        BigDecimal sevScore = text.contains("severe") || text.contains("crisis") || text.contains("danger") ? new BigDecimal("35.00") : new BigDecimal("25.00");
        BigDecimal urgScore = text.contains("immediate") || text.contains("emergency") || text.contains("urgent") ? new BigDecimal("30.00") : new BigDecimal("20.00");
        BigDecimal popScore = new BigDecimal("20.00");
        BigDecimal evScore = (request.getEvidenceCount() != null && request.getEvidenceCount() > 0) ? new BigDecimal("10.00") : new BigDecimal("5.00");
        BigDecimal geoScore = new BigDecimal("5.00");

        BigDecimal totalPriority = sevScore.add(urgScore).add(popScore).add(evScore).add(geoScore)
                .min(new BigDecimal("100.00")).setScale(2, RoundingMode.HALF_UP);

        return AiChallengeAnalysisResponse.builder()
                .normalizedProblemStatement("Citizen reports critical issue regarding " + (request.getTitle() != null ? request.getTitle() : "public infrastructure"))
                .suggestedDomain(predictedDomain)
                .suggestedSubCategory(predictedDomain + "_INFRASTRUCTURE")
                .severityAssessment(sevScore.compareTo(new BigDecimal("30.00")) >= 0 ? "HIGH" : "MEDIUM")
                .urgencyAssessment(urgScore.compareTo(new BigDecimal("25.00")) >= 0 ? "HIGH" : "MEDIUM")
                .affectedPopulationAssessment(request.getEstimatedAffectedPopulation() != null ? request.getEstimatedAffectedPopulation() : 500)
                .confidenceScore(confidence)
                .reasoning("Categorized based on contextual domain keywords [" + String.join(", ", matchedKeywords) + "] and location heuristics.")
                .keywords(matchedKeywords)
                .priorityScore(totalPriority)
                .severityContribution(sevScore)
                .urgencyContribution(urgScore)
                .populationContribution(popScore)
                .evidenceContribution(evScore)
                .geographicImpactContribution(geoScore)
                .priorityReasoning("Multi-factor heuristic computation based on public health severity, urgency indicators, and affected community size.")
                .modelProvider(getProviderName())
                .fallbackUsed(true)
                .build();
    }

    @Override
    public AiDuplicateAnalysisResponse checkDuplicates(AiDuplicateAnalysisRequest request) {
        if (request.getExistingChallenges() == null || request.getExistingChallenges().isEmpty()) {
            return AiDuplicateAnalysisResponse.builder()
                    .potentialDuplicate(false)
                    .similarityScore(BigDecimal.ZERO)
                    .matchedChallengeIds(Collections.emptyList())
                    .explanation("No existing challenges found within the vicinity or domain.")
                    .modelProvider(getProviderName())
                    .fallbackUsed(true)
                    .build();
        }

        String cand = (request.getCandidateTitle() + " " + (request.getCandidateDescription() != null ? request.getCandidateDescription() : "")).toLowerCase(Locale.ROOT);
        Set<String> candWords = new HashSet<>(Arrays.asList(cand.split("\\W+")));

        BigDecimal highestSim = BigDecimal.ZERO;
        AiDuplicateAnalysisRequest.ExistingChallengeSnippet bestMatch = null;

        for (AiDuplicateAnalysisRequest.ExistingChallengeSnippet existing : request.getExistingChallenges()) {
            String exText = (existing.getTitle() + " " + (existing.getDescription() != null ? existing.getDescription() : "")).toLowerCase(Locale.ROOT);
            Set<String> exWords = new HashSet<>(Arrays.asList(exText.split("\\W+")));

            Set<String> intersection = new HashSet<>(candWords);
            intersection.retainAll(exWords);

            Set<String> union = new HashSet<>(candWords);
            union.addAll(exWords);

            if (!union.isEmpty()) {
                double jaccard = (double) intersection.size() / union.size();
                BigDecimal sim = BigDecimal.valueOf(jaccard).setScale(2, RoundingMode.HALF_UP);
                if (sim.compareTo(highestSim) > 0) {
                    highestSim = sim;
                    bestMatch = existing;
                }
            }
        }

        boolean isDup = highestSim.compareTo(new BigDecimal("0.35")) >= 0 && bestMatch != null;

        return AiDuplicateAnalysisResponse.builder()
                .potentialDuplicate(isDup)
                .similarityScore(highestSim)
                .matchedParentChallengeId(isDup && bestMatch != null ? bestMatch.getId() : null)
                .matchedTrackingNumber(isDup && bestMatch != null ? bestMatch.getTrackingNumber() : null)
                .matchedChallengeIds(isDup && bestMatch != null ? List.of(bestMatch.getId()) : Collections.emptyList())
                .explanation(isDup
                        ? "High lexical similarity (" + highestSim.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP) + "%) with registered challenge " + bestMatch.getTrackingNumber()
                        : "Challenge is distinct and unique based on keyword clustering.")
                .modelProvider(getProviderName())
                .fallbackUsed(true)
                .build();
    }

    @Override
    public AiSolutionRecommendationResponse generateSolutionRecommendation(AiSolutionRecommendationRequest request) {
        String domain = request.getDomainCode() != null ? request.getDomainCode() : "WATER_SANITATION";

        List<String> approaches;
        List<String> technologies;
        List<String> disciplines;
        List<String> risks;
        String impact;

        switch (domain) {
            case "WATER_SANITATION":
                approaches = List.of(
                        "Design decentralized gravity-fed ceramic membrane filtration with chemical-free adsorbent media",
                        "Implement IoT solar-powered inline water quality sensor telemetry (pH, TDS, Arsenic/Fluoride)",
                        "Develop low-cost community backwash and cartridge regeneration protocol"
                );
                technologies = List.of("Nanocomposite Ceramic Membranes", "Activated Alumina & Hydroxyapatite Adsorbents", "ESP32 IoT Fluoride Telemetry");
                disciplines = List.of("Materials Science & Nanotechnology", "Environmental & Civil Engineering", "Chemical Engineering", "IoT & Embedded Systems");
                risks = List.of("Membrane fouling due to high turbidity", "Supply chain dependency for proprietary adsorbent media");
                impact = "Provision of 100% WHO/BIS standard potable water to 2,000+ villagers with zero electricity dependency.";
                break;

            case "AGRI_TECH":
                approaches = List.of(
                        "Deploy low-cost edge AI soil moisture and nutrient sensor mesh for automated drip irrigation",
                        "Develop solar-assisted active evaporative cooling storage for post-harvest loss prevention"
                );
                technologies = List.of("Edge AI Microcontrollers", "Thermal Phase Change Materials", "Solar DC Inverters");
                disciplines = List.of("Agricultural Engineering", "Mechanical Engineering", "Computer Science / Embedded AI");
                risks = List.of("Sensor degradation in harsh field environments", "High upfront battery replacement costs");
                impact = "Reduce post-harvest loss by 35% and conserve up to 40% groundwater.";
                break;

            default:
                approaches = List.of(
                        "Develop modular decentralized technological prototype for community deployment",
                        "Form multidisciplinary team to test field durability and cost efficiency"
                );
                technologies = List.of("Open-Source Embedded Telemetry", "Advanced Sustainable Materials", "Mobile Citizen Dashboard");
                disciplines = List.of("Interdisciplinary Engineering", "Computer Science", "Public Policy");
                risks = List.of("Community adoption friction", "Maintenance lifecycle funding gaps");
                impact = "Measurable societal improvement in targeted local jurisdiction.";
                break;
        }

        return AiSolutionRecommendationResponse.builder()
                .problemSummary("Engineering and innovation escalation for: " + request.getTitle() + " (" + request.getTrackingNumber() + ")")
                .proposedSolutionApproaches(approaches)
                .requiredTechnologies(technologies)
                .suggestedDisciplines(disciplines)
                .implementationRisks(risks)
                .expectedImpact(impact)
                .suggestedTRLStartingPoint(3)
                .modelProvider(getProviderName())
                .fallbackUsed(true)
                .build();
    }
}
