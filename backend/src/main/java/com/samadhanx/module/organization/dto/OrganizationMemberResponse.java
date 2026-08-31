package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.OrganizationMember;
import com.samadhanx.module.organization.entity.enums.OrgMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMemberResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private UUID userId;
    private String userName;
    private String userEmail;
    private OrgMemberRole orgRole;
    private String designation;
    private String identifier;
    private boolean verified;
    private Instant joinedAt;

    public static OrganizationMemberResponse fromEntity(OrganizationMember om) {
        if (om == null) return null;

        String orgName = null;
        UUID orgId = null;
        if (om.getOrganization() != null) {
            orgId = om.getOrganization().getId();
            orgName = om.getOrganization().getName();
        }

        String name = null;
        String email = null;
        UUID uId = null;
        if (om.getUser() != null) {
            uId = om.getUser().getId();
            name = om.getUser().getFullName();
            email = om.getUser().getEmail();
        }

        return OrganizationMemberResponse.builder()
                .id(om.getId())
                .organizationId(orgId)
                .organizationName(orgName)
                .userId(uId)
                .userName(name)
                .userEmail(email)
                .orgRole(om.getOrgRole())
                .designation(om.getDesignation())
                .identifier(om.getIdentifier())
                .verified(om.isVerified())
                .joinedAt(om.getJoinedAt())
                .build();
    }
}
