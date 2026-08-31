package com.samadhanx.module.ai.service;

import com.samadhanx.module.ai.dto.AiChallengeAnalysisRequest;
import com.samadhanx.module.ai.dto.AiChallengeAnalysisResponse;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisRequest;
import com.samadhanx.module.ai.dto.AiDuplicateAnalysisResponse;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationRequest;
import com.samadhanx.module.ai.dto.AiSolutionRecommendationResponse;

public interface AiIntelligenceService {

    AiChallengeAnalysisResponse analyzeChallenge(AiChallengeAnalysisRequest request);

    AiDuplicateAnalysisResponse checkDuplicates(AiDuplicateAnalysisRequest request);

    AiSolutionRecommendationResponse generateSolutionRecommendation(AiSolutionRecommendationRequest request);
}
