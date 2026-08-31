package com.samadhanx.module.solution.entity;

import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.user.entity.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Proposal entity for solution proposals addressing Innovation-Required challenges.
 */
@Entity
@Table(name = "proposals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "hackathon_id")
    private UUID hackathonId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "problem_understanding", nullable = false, columnDefinition = "TEXT")
    private String problemUnderstanding;

    @Column(name = "proposed_solution", nullable = false, columnDefinition = "TEXT")
    private String proposedSolution;

    @Column(name = "innovation_novelty", nullable = false, columnDefinition = "TEXT")
    private String innovationNovelty;

    @Column(name = "technical_approach", nullable = false, columnDefinition = "TEXT")
    private String technicalApproach;

    @Column(name = "expected_impact", nullable = false, columnDefinition = "TEXT")
    private String expectedImpact;

    @Column(name = "implementation_plan", nullable = false, columnDefinition = "TEXT")
    private String implementationPlan;

    @Column(name = "required_resources", columnDefinition = "TEXT")
    private String requiredResources;

    @Column(name = "estimated_cost_inr", precision = 15, scale = 2)
    private BigDecimal estimatedCostInr;

    @Column(name = "scalability_plan", columnDefinition = "TEXT")
    private String scalabilityPlan;

    @Column(name = "sustainability_model", columnDefinition = "TEXT")
    private String sustainabilityModel;

    @Column(name = "risk_mitigation", columnDefinition = "TEXT")
    private String riskMitigation;

    @Column(name = "prototype_description", columnDefinition = "TEXT")
    private String prototypeDescription;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private ProposalStatus status = ProposalStatus.PROPOSED;

    @Builder.Default
    @Column(name = "average_score", precision = 5, scale = 2)
    private BigDecimal averageScore = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "evaluation_count")
    private Integer evaluationCount = 0;

    @Builder.Default
    @Column(name = "is_shortlisted", nullable = false)
    private boolean shortlisted = false;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Builder.Default
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();

    @Builder.Default
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProposalDocument> documents = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProposalEvaluation> evaluations = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProposalTimelineEvent> timelineEvents = new HashSet<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void addDocument(ProposalDocument doc) {
        doc.setProposal(this);
        documents.add(doc);
    }

    public void addEvaluation(ProposalEvaluation evaluation) {
        evaluation.setProposal(this);
        evaluations.add(evaluation);
        this.evaluationCount = evaluations.size();
    }

    public void addTimelineEvent(ProposalTimelineEvent event) {
        event.setProposal(this);
        timelineEvents.add(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proposal proposal = (Proposal) o;
        return Objects.equals(id, proposal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
