package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.FacultyProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyProfileResponse {

    private UUID id;
    private UUID userId;
    private String facultyName;
    private String facultyEmail;
    private UUID organizationId;
    private String organizationName;
    private String departmentName;
    private String designation;
    private String academicQualification;
    private String primaryDiscipline;
    private String researchAreas;
    private String patentsSummary;
    private Integer publicationsCount;
    private Integer yearsOfExperience;
    private boolean availableForMentorship;

    public static FacultyProfileResponse fromEntity(FacultyProfile fp) {
        if (fp == null) return null;

        String name = null;
        String email = null;
        if (fp.getUser() != null) {
            name = fp.getUser().getFullName();
            email = fp.getUser().getEmail();
        }

        String orgName = null;
        if (fp.getOrganization() != null) {
            orgName = fp.getOrganization().getName();
        }

        return FacultyProfileResponse.builder()
                .id(fp.getId())
                .userId(fp.getUser() != null ? fp.getUser().getId() : null)
                .facultyName(name)
                .facultyEmail(email)
                .organizationId(fp.getOrganization() != null ? fp.getOrganization().getId() : null)
                .organizationName(orgName)
                .departmentName(fp.getDepartmentName())
                .designation(fp.getDesignation())
                .academicQualification(fp.getAcademicQualification())
                .primaryDiscipline(fp.getPrimaryDiscipline())
                .researchAreas(fp.getResearchAreas())
                .patentsSummary(fp.getPatentsSummary())
                .publicationsCount(fp.getPublicationsCount())
                .yearsOfExperience(fp.getYearsOfExperience())
                .availableForMentorship(fp.isAvailableForMentorship())
                .build();
    }
}
