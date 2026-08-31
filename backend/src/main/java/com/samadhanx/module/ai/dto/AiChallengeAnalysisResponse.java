package com.samadhanx.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChallengeAnalysisResponse {
    private String normalizedProblemStatement;
    private String suggestedDomain;
    private String suggestedSubCategory;
    private String severityAssessment; // LOW, MEDIUM, HIGH, CRITICAL
    private String urgencyAssessment;   // LOW, MEDIUM, HIGH, CRITICAL
    private Integer affectedPopulationAssessment;
    private BigDecimal confidenceScore;
    private String reasoning;
    private List<String> keywords;

    // AI Priority Breakdown
    private BigDecimal priorityScore; // 0 - 100
    private BigDecimal severityContribution;
    private BigDecimal urgencyContribution;
    private BigDecimal populationContribution;
    private BigDecimal evidenceContribution;
    private BigDecimal geographicImpactContribution;
    private String priorityReasoning;

    // Provider Metadata for Explainability
    private String modelProvider;
    private boolean fallbackUsed;
}
