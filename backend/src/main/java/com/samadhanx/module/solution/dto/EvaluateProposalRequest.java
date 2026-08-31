package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.enums.EvaluationRecommendation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateProposalRequest {

    @Schema(example = "90", description = "Problem Understanding (0-100, weight 10%)")
    @NotNull(message = "Problem understanding score is required")
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Integer problemUnderstandingScore;

    @Schema(example = "95", description = "Innovation & Novelty (0-100, weight 20%)")
    @NotNull(message = "Innovation score is required")
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Integer innovationScore;

    @Schema(example = "85", description = "Technical Feasibility (0-100, weight 20%)")
    @NotNull(message = "Technical feasibility score is required")
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Integer technicalFeasibilityScore;

    @Schema(example = "92", description = "Social & Community Impact (0-100, weight 15%)")
    @NotNull(message = "Social impact score is required")
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Integer socialImpactScore;

    @Schema(example = "88", description = "Scalability (0-100, weight 10%)")
    @NotNull(message = "Scalability score is required")
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Integer scalabilityScore;

    @Schema(example = "90", description = "Cost Effectiveness (0-100, weight 10%)")
    @NotNull(message = "Cost effectiveness score is required")
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Integer costEffectivenessScore;

    @Schema(example = "85", description = "Sustainability Model (0-100, weight 5%)")
    @NotNull(message = "Sustainability score is required")
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Integer sustainabilityScore;

    @Schema(example = "85", description = "Implementation Readiness (0-100, weight 10%)")
    @NotNull(message = "Implementation readiness score is required")
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Integer implementationReadinessScore;

    @Schema(example = "Novel zero-electricity ceramic membrane uses local terracotta materials and effective iron-oxide nanoparticle synthesis.")
    private String strengths;

    @Schema(example = "Requires periodic backwash protocols to prevent pore clogging in turbid groundwater.")
    private String weaknesses;

    @Schema(example = "Highly recommended for prototype grant funding and rural village pilot.")
    private String qualitativeFeedback;

    @Schema(example = "SHORTLIST", description = "SHORTLIST, REVISE, REJECT")
    @NotNull(message = "Recommendation is required")
    private EvaluationRecommendation recommendation;
}
