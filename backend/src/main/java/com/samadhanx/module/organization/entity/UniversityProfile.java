package com.samadhanx.module.organization.entity;

import com.samadhanx.module.organization.entity.enums.InstitutionType;
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

import java.util.Objects;
import java.util.UUID;

/**
 * University / HEI profile extension for universities and research institutions.
 */
@Entity
@Table(name = "university_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityProfile implements Persistable<UUID> {

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "aishe_code", unique = true, length = 50)
    private String aisheCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "institution_type", nullable = false, length = 50)
    private InstitutionType institutionType;

    @Column(name = "naac_grade", length = 10)
    private String naacGrade;

    @Column(name = "nirf_rank_range", length = 50)
    private String nirfRankRange;

    @Builder.Default
    @Column(name = "has_incubation_centre", nullable = false)
    private boolean hasIncubationCentre = false;

    @Column(name = "incubation_centre_name", length = 255)
    private String incubationCentreName;

    @Builder.Default
    @Column(name = "total_faculty_count")
    private Integer totalFacultyCount = 0;

    @Builder.Default
    @Column(name = "total_student_count")
    private Integer totalStudentCount = 0;

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
        UniversityProfile that = (UniversityProfile) o;
        return Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId);
    }
}
