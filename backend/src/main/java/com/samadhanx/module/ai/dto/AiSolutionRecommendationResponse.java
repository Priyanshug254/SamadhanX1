package com.samadhanx.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSolutionRecommendationResponse {
    private String problemSummary;
    private List<String> proposedSolutionApproaches;
    private List<String> requiredTechnologies;
    private List<String> suggestedDisciplines;
    private List<String> implementationRisks;
    private String expectedImpact;
    private Integer suggestedTRLStartingPoint; // e.g. 2 or 3
    private String modelProvider;
    private boolean fallbackUsed;
}
