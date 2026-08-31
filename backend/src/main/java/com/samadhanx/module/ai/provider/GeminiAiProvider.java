package com.samadhanx.module.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisRequest;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisResponse;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisRequest;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisResponse;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationRequest;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class GeminiAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiProvider.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${samadhanx.ai.api-key:}")
    private String apiKey;

    @Value("${samadhanx.ai.model:gemini-1.5-flash}")
    private String modelName;

    @Value("${samadhanx.ai.timeout-ms:6000}")
    private int timeoutMs;

    public GeminiAiProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(3000))
                .build();
    }

    @Override
    public String getProviderName() {
        return "GoogleGemini (" + modelName + ")";
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public AiChallengeAnalysisResponse analyzeChallenge(AiChallengeAnalysisRequest request) {
        if (!isConfigured()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        String prompt = """
                You are the SamadhanX National GovTech AI Intelligence Engine.
                Analyze the following crowdsourced citizen societal challenge and return a STRICT JSON object without markdown formatting.
                
                Input:
                Title: %s
                Description: %s
                District: %s, State: %s
                Claimed Domain: %s
                Affected Population: %s
                
                Return JSON schema:
                {
                  "normalizedProblemStatement": "string",
                  "suggestedDomain": "WATER_SANITATION|AGRI_TECH|CLEAN_ENERGY|HEALTHCARE|URBAN_MOBILITY|WASTE_MGMT|DISASTER_RESILIENCE|EDUCATION_SKILLING",
                  "suggestedSubCategory": "string",
                  "severityAssessment": "LOW|MEDIUM|HIGH|CRITICAL",
                  "urgencyAssessment": "LOW|MEDIUM|HIGH|CRITICAL",
                  "affectedPopulationAssessment": number,
                  "confidenceScore": number (between 0.00 and 1.00),
                  "reasoning": "string",
                  "keywords": ["string", "string"],
                  "priorityScore": number (between 0 and 100),
                  "severityContribution": number,
                  "urgencyContribution": number,
                  "populationContribution": number,
                  "evidenceContribution": number,
                  "geographicImpactContribution": number,
                  "priorityReasoning": "string"
                }
                """.formatted(
                request.getTitle(),
                request.getDescription(),
                request.getDistrict(),
                request.getState(),
                request.getClaimedDomainCode(),
                request.getEstimatedAffectedPopulation()
        );

        String jsonText = executeGeminiRequest(prompt);
        try {
            JsonNode root = parseJsonFromModelOutput(jsonText);

            List<String> keywords = new ArrayList<>();
            if (root.has("keywords") && root.get("keywords").isArray()) {
                root.get("keywords").forEach(k -> keywords.add(k.asText()));
            }

            return AiChallengeAnalysisResponse.builder()
                    .normalizedProblemStatement(root.path("normalizedProblemStatement").asText(request.getTitle()))
                    .suggestedDomain(root.path("suggestedDomain").asText("WATER_SANITATION"))
                    .suggestedSubCategory(root.path("suggestedSubCategory").asText("GENERAL_CIVIC"))
                    .severityAssessment(root.path("severityAssessment").asText("MEDIUM"))
                    .urgencyAssessment(root.path("urgencyAssessment").asText("MEDIUM"))
                    .affectedPopulationAssessment(root.path("affectedPopulationAssessment").asInt(500))
                    .confidenceScore(BigDecimal.valueOf(root.path("confidenceScore").asDouble(0.92)).setScale(2, RoundingMode.HALF_UP))
                    .reasoning(root.path("reasoning").asText("Contextual semantic analysis by Gemini LLM"))
                    .keywords(keywords)
                    .priorityScore(BigDecimal.valueOf(root.path("priorityScore").asDouble(75.0)).setScale(2, RoundingMode.HALF_UP))
                    .severityContribution(BigDecimal.valueOf(root.path("severityContribution").asDouble(25.0)))
                    .urgencyContribution(BigDecimal.valueOf(root.path("urgencyContribution").asDouble(25.0)))
                    .populationContribution(BigDecimal.valueOf(root.path("populationContribution").asDouble(15.0)))
                    .evidenceContribution(BigDecimal.valueOf(root.path("evidenceContribution").asDouble(5.0)))
                    .geographicImpactContribution(BigDecimal.valueOf(root.path("geographicImpactContribution").asDouble(5.0)))
                    .priorityReasoning(root.path("priorityReasoning").asText("Evaluated via multi-factor LLM assessment"))
                    .modelProvider(getProviderName())
                    .fallbackUsed(false)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse Gemini output: {}. Raw: {}", e.getMessage(), jsonText);
            throw new RuntimeException("Gemini response parsing failed", e);
        }
    }

    @Override
    public AiDuplicateAnalysisResponse checkDuplicates(AiDuplicateAnalysisRequest request) {
        if (!isConfigured()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        if (request.getExistingChallenges() == null || request.getExistingChallenges().isEmpty()) {
            return AiDuplicateAnalysisResponse.builder()
                    .potentialDuplicate(false)
                    .similarityScore(BigDecimal.ZERO)
                    .matchedChallengeIds(Collections.emptyList())
                    .explanation("No existing candidate challenges within vicinity.")
                    .modelProvider(getProviderName())
                    .fallbackUsed(false)
                    .build();
        }

        StringBuilder candidates = new StringBuilder();
        for (AiDuplicateAnalysisRequest.ExistingChallengeSnippet s : request.getExistingChallenges()) {
            candidates.append(String.format("ID: %s, Tracking: %s, Title: %s, Desc: %s\n", s.getId(), s.getTrackingNumber(), s.getTitle(), s.getDescription()));
        }

        String prompt = """
                You are the SamadhanX Semantic Deduplication Engine.
                Compare the newly submitted challenge against existing candidate challenges and determine if it represents a semantic duplicate.
                
                New Challenge:
                Title: %s
                Description: %s
                District: %s
                
                Existing Candidates:
                %s
                
                Return JSON schema:
                {
                  "potentialDuplicate": boolean,
                  "similarityScore": number (0.00 to 1.00),
                  "matchedParentChallengeId": "UUID or null",
                  "matchedTrackingNumber": "string or null",
                  "matchedChallengeIds": ["UUID"],
                  "explanation": "string"
                }
                """.formatted(request.getCandidateTitle(), request.getCandidateDescription(), request.getDistrict(), candidates.toString());

        String jsonText = executeGeminiRequest(prompt);
        try {
            JsonNode root = parseJsonFromModelOutput(jsonText);
            boolean isDup = root.path("potentialDuplicate").asBoolean(false);
            double sim = root.path("similarityScore").asDouble(0.0);
            String matchedIdStr = root.path("matchedParentChallengeId").asText(null);
            UUID matchedId = (matchedIdStr != null && !matchedIdStr.isBlank() && !matchedIdStr.equals("null"))
                    ? UUID.fromString(matchedIdStr) : null;

            return AiDuplicateAnalysisResponse.builder()
                    .potentialDuplicate(isDup)
                    .similarityScore(BigDecimal.valueOf(sim).setScale(2, RoundingMode.HALF_UP))
                    .matchedParentChallengeId(matchedId)
                    .matchedTrackingNumber(root.path("matchedTrackingNumber").asText(null))
                    .matchedChallengeIds(matchedId != null ? List.of(matchedId) : Collections.emptyList())
                    .explanation(root.path("explanation").asText("Semantic similarity evaluated by Gemini"))
                    .modelProvider(getProviderName())
                    .fallbackUsed(false)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse Gemini duplicate output: {}", e.getMessage());
            throw new RuntimeException("Gemini duplicate parsing failed", e);
        }
    }

    @Override
    public AiSolutionRecommendationResponse generateSolutionRecommendation(AiSolutionRecommendationRequest request) {
        if (!isConfigured()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        String prompt = """
                You are the SamadhanX University & Research Innovation Intelligence Advisor.
                Generate a structured R&D and solution roadmap for a societal challenge escalated to INNOVATION_REQUIRED.
                
                Challenge Details:
                Tracking: %s
                Title: %s
                Description: %s
                Domain: %s
                Escalation Reason: %s
                District: %s, State: %s
                
                Return JSON schema:
                {
                  "problemSummary": "string",
                  "proposedSolutionApproaches": ["string", "string", "string"],
                  "requiredTechnologies": ["string", "string"],
                  "suggestedDisciplines": ["string", "string"],
                  "implementationRisks": ["string", "string"],
                  "expectedImpact": "string",
                  "suggestedTRLStartingPoint": number (1 to 9)
                }
                """.formatted(
                request.getTrackingNumber(),
                request.getTitle(),
                request.getDescription(),
                request.getDomainName(),
                request.getEscalationReason(),
                request.getDistrict(),
                request.getState()
        );

        String jsonText = executeGeminiRequest(prompt);
        try {
            JsonNode root = parseJsonFromModelOutput(jsonText);

            List<String> approaches = new ArrayList<>();
            if (root.has("proposedSolutionApproaches")) {
                root.get("proposedSolutionApproaches").forEach(a -> approaches.add(a.asText()));
            }

            List<String> technologies = new ArrayList<>();
            if (root.has("requiredTechnologies")) {
                root.get("requiredTechnologies").forEach(t -> technologies.add(t.asText()));
            }

            List<String> disciplines = new ArrayList<>();
            if (root.has("suggestedDisciplines")) {
                root.get("suggestedDisciplines").forEach(d -> disciplines.add(d.asText()));
            }

            List<String> risks = new ArrayList<>();
            if (root.has("implementationRisks")) {
                root.get("implementationRisks").forEach(r -> risks.add(r.asText()));
            }

            return AiSolutionRecommendationResponse.builder()
                    .problemSummary(root.path("problemSummary").asText("Innovative solution blueprint for " + request.getTitle()))
                    .proposedSolutionApproaches(approaches)
                    .requiredTechnologies(technologies)
                    .suggestedDisciplines(disciplines)
                    .implementationRisks(risks)
                    .expectedImpact(root.path("expectedImpact").asText("High community-level impact"))
                    .suggestedTRLStartingPoint(root.path("suggestedTRLStartingPoint").asInt(3))
                    .modelProvider(getProviderName())
                    .fallbackUsed(false)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse Gemini solution output: {}", e.getMessage());
            throw new RuntimeException("Gemini solution recommendation parsing failed", e);
        }
    }

    private String executeGeminiRequest(String promptText) {
        try {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    modelName, apiKey
            );

            // Construct Gemini Request JSON
            String requestBody = objectMapper.writeValueAsString(
                    Collections.singletonMap("contents", List.of(
                            Collections.singletonMap("parts", List.of(
                                    Collections.singletonMap("text", promptText)
                            ))
                    ))
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(timeoutMs))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Gemini API call returned HTTP {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("Gemini API returned error code " + response.statusCode());
            }

            JsonNode respNode = objectMapper.readTree(response.body());
            JsonNode candidate = respNode.path("candidates").get(0);
            return candidate.path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            log.warn("Gemini HTTP call failed: {}", e.getMessage());
            throw new RuntimeException("Gemini execution failed", e);
        }
    }

    private JsonNode parseJsonFromModelOutput(String rawOutput) throws Exception {
        String clean = rawOutput.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        return objectMapper.readTree(clean.trim());
    }
}
