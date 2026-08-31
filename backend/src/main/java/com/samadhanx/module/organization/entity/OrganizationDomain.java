package com.samadhanx.module.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "organization_domain_mappings",
        uniqueConstraints = @UniqueConstraint(name = "uq_org_domain", columnNames = {"organization_id", "domain_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationDomain that = (OrganizationDomain) o;
        return Objects.equals(organization != null ? organization.getId() : null, that.organization != null ? that.organization.getId() : null) &&
                Objects.equals(domain != null ? domain.getId() : null, that.domain != null ? that.domain.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization != null ? organization.getId() : null, domain != null ? domain.getId() : null);
    }
}
