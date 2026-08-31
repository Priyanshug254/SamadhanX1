package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentProfileResponse {

    private UUID organizationId;
    private UUID parentDepartmentId;
    private String parentDepartmentName;
    private GovernmentLevel level;
    private String jurisdictionArea;
    private String nodalOfficerName;
    private String nodalOfficerEmail;
    private String nodalOfficerPhone;
    private List<ProblemCategoryResponse> problemCategories;

    public static DepartmentProfileResponse fromEntity(Department dept) {
        if (dept == null) return null;

        List<ProblemCategoryResponse> categories = null;
        if (dept.getProblemCategories() != null) {
            categories = dept.getProblemCategories().stream()
                    .map(ProblemCategoryResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        String parentName = null;
        UUID parentId = null;
        if (dept.getParentDepartment() != null) {
            parentId = dept.getParentDepartment().getOrganizationId();
            if (dept.getParentDepartment().getOrganization() != null) {
                parentName = dept.getParentDepartment().getOrganization().getName();
            }
        }

        return DepartmentProfileResponse.builder()
                .organizationId(dept.getOrganizationId())
                .parentDepartmentId(parentId)
                .parentDepartmentName(parentName)
                .level(dept.getLevel())
                .jurisdictionArea(dept.getJurisdictionArea())
                .nodalOfficerName(dept.getNodalOfficerName())
                .nodalOfficerEmail(dept.getNodalOfficerEmail())
                .nodalOfficerPhone(dept.getNodalOfficerPhone())
                .problemCategories(categories)
                .build();
    }
}
