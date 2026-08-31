package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private UUID id;
    private String name;
    private String code;
    private OrganizationType organizationType;
    private String description;
    private String website;
    private String contactEmail;
    private String contactPhone;
    private String addressLine;
    private String district;
    private String state;
    private String pincode;
    private VerificationStatus verificationStatus;
    private Instant verifiedAt;
    private UUID verifiedBy;
    private String rejectionReason;
    private List<OrganizationDomainDto> domains;
    private DepartmentProfileResponse department;
    private UniversityProfileResponse universityProfile;
    private IndustryProfileResponse industryProfile;
    private int memberCount;
    private int resourceCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrganizationResponse fromEntity(Organization org) {
        if (org == null) return null;

        List<OrganizationDomainDto> domainDtos = null;
        if (org.getDomainMappings() != null) {
            domainDtos = org.getDomainMappings().stream()
                    .map(OrganizationDomainDto::fromEntity)
                    .collect(Collectors.toList());
        }

        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .code(org.getCode())
                .organizationType(org.getOrganizationType())
                .description(org.getDescription())
                .website(org.getWebsite())
                .contactEmail(org.getContactEmail())
                .contactPhone(org.getContactPhone())
                .addressLine(org.getAddressLine())
                .district(org.getDistrict())
                .state(org.getState())
                .pincode(org.getPincode())
                .verificationStatus(org.getVerificationStatus())
                .verifiedAt(org.getVerifiedAt())
                .verifiedBy(org.getVerifiedBy())
                .rejectionReason(org.getRejectionReason())
                .domains(domainDtos)
                .department(DepartmentProfileResponse.fromEntity(org.getDepartment()))
                .universityProfile(UniversityProfileResponse.fromEntity(org.getUniversityProfile()))
                .industryProfile(IndustryProfileResponse.fromEntity(org.getIndustryProfile()))
                .memberCount(org.getMembers() != null ? org.getMembers().size() : 0)
                .resourceCount(org.getResources() != null ? org.getResources().size() : 0)
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}
