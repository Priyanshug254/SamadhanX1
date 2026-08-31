package com.samadhanx.module.partnership.entity;

import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.partnership.entity.enums.LicensingType;
import com.samadhanx.module.partnership.entity.enums.TechTransferDeploymentStatus;
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

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tech_transfer_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechTransferRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Column(name = "ip_registration_number", length = 100)
    private String ipRegistrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "licensing_type", nullable = false, length = 50)
    private LicensingType licensingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_organization_id", nullable = false)
    private Organization receivingOrganization;

    @Builder.Default
    @Column(name = "transfer_date", nullable = false)
    private Instant transferDate = Instant.now();

    @Column(name = "responsible_parties", nullable = false, columnDefinition = "TEXT")
    private String responsibleParties;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "deployment_status", nullable = false, length = 50)
    private TechTransferDeploymentStatus deploymentStatus = TechTransferDeploymentStatus.TRANSFERRED;

    @Column(name = "documentation_url", length = 500)
    private String documentationUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TechTransferRecord that = (TechTransferRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
