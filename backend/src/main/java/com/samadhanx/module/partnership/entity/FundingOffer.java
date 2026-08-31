package com.samadhanx.module.partnership.entity;

import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.partnership.entity.enums.FundingOfferStatus;
import com.samadhanx.module.partnership.entity.enums.FundingSupportType;
import com.samadhanx.module.solution.entity.Proposal;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "funding_offers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundingOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id", nullable = false)
    private FundingRequirement requirement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_organization_id", nullable = false)
    private Organization sponsorOrganization;

    @Column(name = "offered_amount_inr", precision = 15, scale = 2, nullable = false)
    private BigDecimal offeredAmountInr;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_type", nullable = false, length = 50)
    private FundingSupportType supportType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FundingOfferStatus status = FundingOfferStatus.REQUESTED;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Builder.Default
    @Column(name = "disbursed_amount_inr", precision = 15, scale = 2)
    private BigDecimal disbursedAmountInr = BigDecimal.ZERO;

    @Column(name = "disbursed_at")
    private Instant disbursedAt;

    @Column(name = "utilization_report", columnDefinition = "TEXT")
    private String utilizationReport;

    @Column(name = "evidence_document_url", length = 500)
    private String evidenceDocumentUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FundingOffer that = (FundingOffer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
