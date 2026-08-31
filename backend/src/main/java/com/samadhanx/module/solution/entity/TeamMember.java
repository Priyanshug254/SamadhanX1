package com.samadhanx.module.solution.entity;

import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.solution.entity.enums.TeamMemberStatus;
import com.samadhanx.module.solution.entity.enums.TeamRole;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * TeamMember entity capturing members, faculty mentors, students, and inter-university collaborators.
 */
@Entity
@Table(
        name = "team_members",
        uniqueConstraints = @UniqueConstraint(name = "uq_team_user", columnNames = {"team_id", "user_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false)
    private Organization university;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_role", nullable = false, length = 40)
    private TeamRole teamRole;

    @Column(name = "academic_discipline", length = 100)
    private String academicDiscipline;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TeamMemberStatus status = TeamMemberStatus.INVITED;

    @Column(name = "invitation_notes", length = 255)
    private String invitationNotes;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMember that = (TeamMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
