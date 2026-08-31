package com.samadhanx.module.challenge.entity;

import com.samadhanx.common.entity.BaseAuditEntity;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.entity.enums.ResolutionPath;
import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.SubmitterType;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;
import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
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
 * Challenge Master Entity for SIH Problem Statement 26043.
 * Captures crowdsourced societal challenges with GIS geotagging, AI categorization,
 * priority scoring, deduplication clusters, and full department/academic lifecycle tracking.
 */
@Entity
@Table(name = "challenges")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Challenge extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // ── Submitter ─────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "submitter_type", nullable = false, length = 50)
    private SubmitterType submitterType;

    // ── Domain & AI Categorization ────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_predicted_domain_id")
    private Domain aiPredictedDomain;

    @Column(name = "ai_confidence_score", precision = 4, scale = 3)
    private BigDecimal aiConfidenceScore;

    @Column(name = "ai_keywords", columnDefinition = "TEXT")
    private String aiKeywords;

    @Column(name = "ai_reasoning", columnDefinition = "TEXT")
    private String aiReasoning;

    @Column(name = "ai_priority_reasoning", columnDefinition = "TEXT")
    private String aiPriorityReasoning;

    @Column(name = "ai_duplicate_explanation", columnDefinition = "TEXT")
    private String aiDuplicateExplanation;

    @Column(name = "ai_solution_recommendation", columnDefinition = "TEXT")
    private String aiSolutionRecommendation;

    @Column(name = "ai_model_provider", length = 100)
    private String aiModelProvider;

    // ── Geospatial & Location ─────────────────────────────────
    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "locality", length = 100)
    private String locality;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(name = "jurisdiction_level", nullable = false, length = 30)
    private GovernmentLevel jurisdictionLevel;

    // ── Priority & Impact Estimation ──────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false, length = 20)
    private SeverityLevel severityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false, length = 20)
    private UrgencyLevel urgencyLevel;

    @Builder.Default
    @Column(name = "estimated_affected_pop")
    private Integer estimatedAffectedPopulation = 0;

    @Builder.Default
    @Column(name = "priority_score", precision = 5, scale = 2)
    private BigDecimal priorityScore = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "endorsement_count")
    private Integer endorsementCount = 0;

    // ── Deduplication & Clustering ────────────────────────────
    @Column(name = "cluster_id")
    private UUID clusterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_challenge_id")
    private Challenge parentChallenge;

    @Builder.Default
    @Column(name = "is_duplicate", nullable = false)
    private boolean duplicate = false;

    @Column(name = "duplicate_similarity", precision = 4, scale = 3)
    private BigDecimal duplicateSimilarity;

    // ── Lifecycle State & Resolution Path ─────────────────────
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private ChallengeStatus status = ChallengeStatus.SUBMITTED;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_path", nullable = false, length = 40)
    private ResolutionPath resolutionPath = ResolutionPath.PENDING_TRIAGE;

    // ── Department Assignment ─────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_department_id")
    private Department assignedDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer;

    @Column(name = "routing_rationale", columnDefinition = "TEXT")
    private String routingRationale;

    @Column(name = "target_resolution_date")
    private Instant targetResolutionDate;

    // ── Resolution & Closure ──────────────────────────────────
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolution_summary", columnDefinition = "TEXT")
    private String resolutionSummary;

    @Column(name = "measurable_impact_desc", columnDefinition = "TEXT")
    private String measurableImpactDescription;

    // ── Sub-collections ───────────────────────────────────────
    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ChallengeAttachment> attachments = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ChallengeEndorsement> endorsements = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ChallengeDepartmentAction> departmentActions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ChallengeTimelineEvent> timelineEvents = new HashSet<>();

    public void addAttachment(ChallengeAttachment attachment) {
        attachment.setChallenge(this);
        attachments.add(attachment);
    }

    public void addEndorsement(ChallengeEndorsement endorsement) {
        endorsement.setChallenge(this);
        endorsements.add(endorsement);
        this.endorsementCount = endorsements.size();
    }

    public void addDepartmentAction(ChallengeDepartmentAction action) {
        action.setChallenge(this);
        departmentActions.add(action);
    }

    public void addTimelineEvent(ChallengeTimelineEvent event) {
        event.setChallenge(this);
        timelineEvents.add(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Challenge challenge = (Challenge) o;
        return Objects.equals(id, challenge.id) || (trackingNumber != null && Objects.equals(trackingNumber, challenge.trackingNumber));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, trackingNumber);
    }
}
