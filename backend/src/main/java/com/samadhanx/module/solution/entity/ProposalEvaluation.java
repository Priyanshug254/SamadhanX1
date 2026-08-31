package com.samadhanx.module.solution.entity;

import com.samadhanx.module.solution.entity.enums.EvaluationRecommendation;
import com.samadhanx.module.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * ProposalEvaluation entity capturing multi-dimensional scoring and qualitative feedback.
 */
@Entity
@Table(
        name = "proposal_evaluations",
        uniqueConstraints = @UniqueConstraint(name = "uq_proposal_evaluator", columnNames = {"proposal_id", "evaluator_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @Column(name = "problem_understanding_score", nullable = false)
    private Integer problemUnderstandingScore; // 0-100

    @Column(name = "innovation_score", nullable = false)
    private Integer innovationScore; // 0-100

    @Column(name = "technical_feasibility_score", nullable = false)
    private Integer technicalFeasibilityScore; // 0-100

    @Column(name = "social_impact_score", nullable = false)
    private Integer socialImpactScore; // 0-100

    @Column(name = "scalability_score", nullable = false)
    private Integer scalabilityScore; // 0-100

    @Column(name = "cost_effectiveness_score", nullable = false)
    private Integer costEffectivenessScore; // 0-100

    @Column(name = "sustainability_score", nullable = false)
    private Integer sustainabilityScore; // 0-100

    @Column(name = "implementation_readiness_score", nullable = false)
    private Integer implementationReadinessScore; // 0-100

    @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore; // 0.00 - 100.00

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "qualitative_feedback", columnDefinition = "TEXT")
    private String qualitativeFeedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false, length = 30)
    private EvaluationRecommendation recommendation;

    @Column(name = "scoring_rationale", columnDefinition = "TEXT")
    private String scoringRationale;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProposalEvaluation that = (ProposalEvaluation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
