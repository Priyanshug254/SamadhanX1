package com.samadhanx.module.partnership.entity;

import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.partnership.entity.enums.MentorshipStatus;
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
@Table(name = "mentorship_engagements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorshipEngagement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_user_id", nullable = false)
    private User mentorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_organization_id")
    private Organization mentorOrganization;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "mentorship_status", nullable = false, length = 30)
    private MentorshipStatus mentorshipStatus = MentorshipStatus.INVITED;

    @Column(name = "goals_and_objectives", columnDefinition = "TEXT")
    private String goalsAndObjectives;

    @Column(name = "invitation_notes", columnDefinition = "TEXT")
    private String invitationNotes;

    @Builder.Default
    @OneToMany(mappedBy = "engagement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MentorshipLog> activityLogs = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void addLog(MentorshipLog log) {
        activityLogs.add(log);
        log.setEngagement(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MentorshipEngagement that = (MentorshipEngagement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
