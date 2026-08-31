package com.samadhanx.module.partnership.entity;

import com.samadhanx.module.organization.entity.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "partner_capabilities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCapability implements Persistable<UUID> {

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "sectors", columnDefinition = "TEXT")
    private String sectors;

    @Column(name = "technologies", columnDefinition = "TEXT")
    private String technologies;

    @Column(name = "areas_of_interest", columnDefinition = "TEXT")
    private String areasOfInterest;

    @Builder.Default
    @Column(name = "mentoring_capability")
    private boolean mentoringCapability = false;

    @Builder.Default
    @Column(name = "funding_capability")
    private boolean fundingCapability = false;

    @Builder.Default
    @Column(name = "prototyping_capability")
    private boolean prototypingCapability = false;

    @Builder.Default
    @Column(name = "testing_capability")
    private boolean testingCapability = false;

    @Builder.Default
    @Column(name = "deployment_capability")
    private boolean deploymentCapability = false;

    @Column(name = "geographic_service_areas", length = 500)
    private String geographicServiceAreas;

    @Builder.Default
    @Column(name = "available_resources_budget", precision = 15, scale = 2)
    private BigDecimal availableResourcesBudget = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

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
        PartnerCapability that = (PartnerCapability) o;
        return Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId);
    }
}
