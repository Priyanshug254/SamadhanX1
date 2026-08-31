package com.samadhanx.module.partnership.entity;

import com.samadhanx.module.partnership.entity.enums.CoDevMilestoneStatus;
import com.samadhanx.module.partnership.entity.enums.LeadParty;
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
@Table(name = "co_dev_milestones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoDevMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private CoDevelopmentProject project;

    @Column(name = "milestone_name", nullable = false)
    private String milestoneName;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_party", nullable = false, length = 50)
    private LeadParty leadParty;

    @Column(name = "deliverables", nullable = false, columnDefinition = "TEXT")
    private String deliverables;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "completion_date")
    private Instant completionDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CoDevMilestoneStatus status = CoDevMilestoneStatus.PLANNED;

    @Column(name = "documentation_url", length = 500)
    private String documentationUrl;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoDevMilestone that = (CoDevMilestone) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
