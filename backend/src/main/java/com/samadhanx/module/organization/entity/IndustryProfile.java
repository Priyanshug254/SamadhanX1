package com.samadhanx.module.organization.entity;

import com.samadhanx.module.organization.entity.enums.CompanyStage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Industry Profile extension for Corporates, Startups, MSMEs, and CSR Partners.
 */
@Entity
@Table(name = "industry_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryProfile implements Persistable<UUID> {

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Builder.Default
    @Column(name = "dpiit_recognized")
    private boolean dpiitRecognized = false;

    @Column(name = "dpiit_number", length = 50)
    private String dpiitNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_stage", length = 50)
    private CompanyStage companyStage;

    @Column(name = "offering_types", length = 255)
    private String offeringTypes;

    @Column(name = "annual_csr_budget_inr", precision = 15, scale = 2)
    private BigDecimal annualCsrBudgetInr;

    @Column(name = "focus_sectors", columnDefinition = "TEXT")
    private String focusSectors;

    @Builder.Default
    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return organizationId;
    }

    @Override
    public boolean isNew() {
        return isNew || organizationId == null;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndustryProfile that = (IndustryProfile) o;
        return Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId);
    }
}
