package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.partnership.entity.PartnerCapability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCapabilityResponse {

    private UUID organizationId;
    private String organizationName;
    private String organizationCode;
    private OrganizationType organizationType;
    private String sectors;
    private String technologies;
    private String areasOfInterest;
    private boolean mentoringCapability;
    private boolean fundingCapability;
    private boolean prototypingCapability;
    private boolean testingCapability;
    private boolean deploymentCapability;
    private String geographicServiceAreas;
    private BigDecimal availableResourcesBudget;
    private Instant createdAt;

    public static PartnerCapabilityResponse fromEntity(PartnerCapability pc) {
        if (pc == null) return null;
        return PartnerCapabilityResponse.builder()
                .organizationId(pc.getOrganizationId())
                .organizationName(pc.getOrganization() != null ? pc.getOrganization().getName() : null)
                .organizationCode(pc.getOrganization() != null ? pc.getOrganization().getCode() : null)
                .organizationType(pc.getOrganization() != null ? pc.getOrganization().getOrganizationType() : null)
                .sectors(pc.getSectors())
                .technologies(pc.getTechnologies())
                .areasOfInterest(pc.getAreasOfInterest())
                .mentoringCapability(pc.isMentoringCapability())
                .fundingCapability(pc.isFundingCapability())
                .prototypingCapability(pc.isPrototypingCapability())
                .testingCapability(pc.isTestingCapability())
                .deploymentCapability(pc.isDeploymentCapability())
                .geographicServiceAreas(pc.getGeographicServiceAreas())
                .availableResourcesBudget(pc.getAvailableResourcesBudget())
                .createdAt(pc.getCreatedAt())
                .build();
    }
}
