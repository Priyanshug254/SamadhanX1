package com.samadhanx.module.organization.entity;

import com.samadhanx.module.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

/**
 * FacultyProfile entity containing academic disciplines, research areas, and mentorship availability.
 * Designed for future AI challenge-matching with academic innovators.
 */
@Entity
@Table(name = "faculty_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "academic_qualification", length = 100)
    private String academicQualification;

    @Column(name = "primary_discipline", nullable = false, length = 100)
    private String primaryDiscipline;

    @Column(name = "research_areas", columnDefinition = "TEXT")
    private String researchAreas;

    @Column(name = "patents_summary", columnDefinition = "TEXT")
    private String patentsSummary;

    @Builder.Default
    @Column(name = "publications_count")
    private Integer publicationsCount = 0;

    @Builder.Default
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience = 0;

    @Builder.Default
    @Column(name = "is_available_for_mentorship", nullable = false)
    private boolean availableForMentorship = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacultyProfile that = (FacultyProfile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
