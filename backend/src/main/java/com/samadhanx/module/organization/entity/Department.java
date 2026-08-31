package com.samadhanx.module.organization.entity;

import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Department entity extending Organization for Government departments & bodies.
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department implements Persistable<UUID> {

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @Builder.Default
    @OneToMany(mappedBy = "parentDepartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Department> subDepartments = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 30)
    private GovernmentLevel level;

    @Column(name = "jurisdiction_area", nullable = false, length = 255)
    private String jurisdictionArea;

    @Column(name = "nodal_officer_name", length = 100)
    private String nodalOfficerName;

    @Column(name = "nodal_officer_email", length = 255)
    private String nodalOfficerEmail;

    @Column(name = "nodal_officer_phone", length = 20)
    private String nodalOfficerPhone;

    @Builder.Default
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DepartmentProblemCategory> problemCategories = new HashSet<>();

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

    public void addProblemCategory(String categoryName, String description, Integer resolutionDays) {
        DepartmentProblemCategory category = DepartmentProblemCategory.builder()
                .department(this)
                .categoryName(categoryName)
                .description(description)
                .typicalResolutionDays(resolutionDays != null ? resolutionDays : 14)
                .build();
        problemCategories.add(category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId);
    }
}
