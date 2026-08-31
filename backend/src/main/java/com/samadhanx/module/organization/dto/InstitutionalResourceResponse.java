package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.InstitutionalResource;
import com.samadhanx.module.organization.entity.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionalResourceResponse {

    private UUID id;
    private UUID organizationId;
    private String resourceName;
    private ResourceType resourceType;
    private String description;
    private String equipmentList;
    private boolean accessibleToExternalTeams;

    public static InstitutionalResourceResponse fromEntity(InstitutionalResource res) {
        if (res == null) return null;
        return InstitutionalResourceResponse.builder()
                .id(res.getId())
                .organizationId(res.getOrganization() != null ? res.getOrganization().getId() : null)
                .resourceName(res.getResourceName())
                .resourceType(res.getResourceType())
                .description(res.getDescription())
                .equipmentList(res.getEquipmentList())
                .accessibleToExternalTeams(res.isAccessibleToExternalTeams())
                .build();
    }
}
