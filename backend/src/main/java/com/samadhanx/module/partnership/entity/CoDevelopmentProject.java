package com.samadhanx.module.partnership.entity;

import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.partnership.entity.enums.CoDevStatus;
import com.samadhanx.module.solution.entity.Proposal;
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
@Table(name = "co_development_projects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoDevelopmentProject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_organization_id", nullable = false)
    private Organization partnerOrganization;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "objectives", nullable = false, columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "lead_academic_coordinator", length = 150)
    private String leadAcademicCoordinator;

    @Column(name = "lead_industry_coordinator", length = 150)
    private String leadIndustryCoordinator;

    @Builder.Default
    @Column(name = "start_date", nullable = false)
    private Instant startDate = Instant.now();

    @Column(name = "target_completion_date")
    private Instant targetCompletionDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CoDevStatus status = CoDevStatus.ACTIVE;

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoDevMilestone> milestones = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void addMilestone(CoDevMilestone milestone) {
        milestones.add(milestone);
        milestone.setProject(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoDevelopmentProject that = (CoDevelopmentProject) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
