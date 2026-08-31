package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.UniversityProfile;
import com.samadhanx.module.organization.entity.enums.InstitutionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityProfileResponse {

    private UUID organizationId;
    private String aisheCode;
    private InstitutionType institutionType;
    private String naacGrade;
    private String nirfRankRange;
    private boolean hasIncubationCentre;
    private String incubationCentreName;
    private Integer totalFacultyCount;
    private Integer totalStudentCount;

    public static UniversityProfileResponse fromEntity(UniversityProfile up) {
        if (up == null) return null;
        return UniversityProfileResponse.builder()
                .organizationId(up.getOrganizationId())
                .aisheCode(up.getAisheCode())
                .institutionType(up.getInstitutionType())
                .naacGrade(up.getNaacGrade())
                .nirfRankRange(up.getNirfRankRange())
                .hasIncubationCentre(up.isHasIncubationCentre())
                .incubationCentreName(up.getIncubationCentreName())
                .totalFacultyCount(up.getTotalFacultyCount())
                .totalStudentCount(up.getTotalStudentCount())
                .build();
    }
}
