package com.samadhanx.module.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisRequest;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisResponse;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationRequest;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationResponse;
import com.samadhanx.module.ai.service.AiIntelligenceService;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Intelligence Layer", description = "LLM-assisted challenge categorization, priority intelligence, duplicate clustering, and R&D solution recommendations")
public class AiController {

    private final AiIntelligenceService aiIntelligenceService;
    private final ChallengeRepository challengeRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/analyze-preview")
    @Operation(summary = "Preview AI challenge understanding and priority scoring")
    public ResponseEntity<ApiResponse<AiChallengeAnalysisResponse>> analyzePreview(
            @RequestBody AiChallengeAnalysisRequest request
    ) {
        AiChallengeAnalysisResponse response = aiIntelligenceService.analyzeChallenge(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/challenges/{id}/recommendation")
    @Operation(summary = "Get structured AI Solution Recommendation for an Innovation Challenge")
    public ResponseEntity<ApiResponse<AiSolutionRecommendationResponse>> getSolutionRecommendation(
            @PathVariable("id") UUID challengeId
    ) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found with ID: " + challengeId));

        if (challenge.getAiSolutionRecommendation() != null && !challenge.getAiSolutionRecommendation().isBlank()) {
            try {
                AiSolutionRecommendationResponse cached = objectMapper.readValue(
                        challenge.getAiSolutionRecommendation(),
                        AiSolutionRecommendationResponse.class
                );
                return ResponseEntity.ok(ApiResponse.success(cached));
            } catch (Exception ignored) {}
        }

        AiSolutionRecommendationResponse fresh = aiIntelligenceService.generateSolutionRecommendation(
                AiSolutionRecommendationRequest.builder()
                        .challengeId(challenge.getId())
                        .trackingNumber(challenge.getTrackingNumber())
                        .title(challenge.getTitle())
                        .description(challenge.getDescription())
                        .domainCode(challenge.getDomain() != null ? challenge.getDomain().getCode() : null)
                        .domainName(challenge.getDomain() != null ? challenge.getDomain().getName() : null)
                        .district(challenge.getDistrict())
                        .state(challenge.getState())
                        .build()
        );

        try {
            challenge.setAiSolutionRecommendation(objectMapper.writeValueAsString(fresh));
            challengeRepository.save(challenge);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(ApiResponse.success(fresh));
    }
}
