package com.samadhanx.module.partnership.entity;

import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.partnership.entity.enums.CommunityValidationStatus;
import com.samadhanx.module.partnership.entity.enums.PilotStatus;
import com.samadhanx.module.solution.entity.Proposal;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pilot_projects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PilotProject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @Column(name = "pilot_code", nullable = false, unique = true, length = 50)
    private String pilotCode;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Builder.Default
    @Column(name = "target_population", nullable = false)
    private Integer targetPopulation = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "implementation_partner_id")
    private Organization implementationPartner;

    @Builder.Default
    @Column(name = "start_date", nullable = false)
    private Instant startDate = Instant.now();

    @Column(name = "expected_end_date")
    private Instant expectedEndDate;

    @Column(name = "actual_end_date")
    private Instant actualEndDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PilotStatus status = PilotStatus.PLANNED;

    @Column(name = "objectives", nullable = false, columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "feedback_notes", columnDefinition = "TEXT")
    private String feedbackNotes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "community_validation_status", nullable = false, length = 30)
    private CommunityValidationStatus communityValidationStatus = CommunityValidationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder.Default
    @OneToMany(mappedBy = "pilot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImpactMetric> impactMetrics = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void addMetric(ImpactMetric metric) {
        impactMetrics.add(metric);
        metric.setPilot(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PilotProject that = (PilotProject) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
