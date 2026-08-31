package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.ProposalEvaluation;
import com.samadhanx.module.solution.entity.enums.EvaluationRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalEvaluationResponse {

    private UUID id;
    private UUID proposalId;
    private UUID evaluatorId;
    private String evaluatorName;
    private Integer problemUnderstandingScore;
    private Integer innovationScore;
    private Integer technicalFeasibilityScore;
    private Integer socialImpactScore;
    private Integer scalabilityScore;
    private Integer costEffectivenessScore;
    private Integer sustainabilityScore;
    private Integer implementationReadinessScore;
    private BigDecimal totalScore;
    private String strengths;
    private String weaknesses;
    private String qualitativeFeedback;
    private EvaluationRecommendation recommendation;
    private String scoringRationale;
    private Instant createdAt;

    public static ProposalEvaluationResponse fromEntity(ProposalEvaluation pe) {
        if (pe == null) return null;

        String eName = null;
        UUID eId = null;
        if (pe.getEvaluator() != null) {
            eId = pe.getEvaluator().getId();
            eName = pe.getEvaluator().getFullName();
        }

        return ProposalEvaluationResponse.builder()
                .id(pe.getId())
                .proposalId(pe.getProposal() != null ? pe.getProposal().getId() : null)
                .evaluatorId(eId)
                .evaluatorName(eName)
                .problemUnderstandingScore(pe.getProblemUnderstandingScore())
                .innovationScore(pe.getInnovationScore())
                .technicalFeasibilityScore(pe.getTechnicalFeasibilityScore())
                .socialImpactScore(pe.getSocialImpactScore())
                .scalabilityScore(pe.getScalabilityScore())
                .costEffectivenessScore(pe.getCostEffectivenessScore())
                .sustainabilityScore(pe.getSustainabilityScore())
                .implementationReadinessScore(pe.getImplementationReadinessScore())
                .totalScore(pe.getTotalScore())
                .strengths(pe.getStrengths())
                .weaknesses(pe.getWeaknesses())
                .qualitativeFeedback(pe.getQualitativeFeedback())
                .recommendation(pe.getRecommendation())
                .scoringRationale(pe.getScoringRationale())
                .createdAt(pe.getCreatedAt())
                .build();
    }
}
