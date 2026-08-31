package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.partnership.entity.PilotProject;
import com.samadhanx.module.partnership.entity.enums.CommunityValidationStatus;
import com.samadhanx.module.partnership.entity.enums.PilotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PilotProjectResponse {

    private UUID id;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private String pilotCode;
    private String locationName;
    private String district;
    private String state;
    private String pincode;
    private int targetPopulation;
    private UUID implementationPartnerId;
    private String implementationPartnerName;
    private OrganizationType implementationPartnerType;
    private Instant startDate;
    private Instant expectedEndDate;
    private Instant actualEndDate;
    private PilotStatus status;
    private String objectives;
    private String feedbackNotes;
    private CommunityValidationStatus communityValidationStatus;
    
    @Builder.Default
    private List<ImpactMetricResponse> impactMetrics = new ArrayList<>();
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;

    public static PilotProjectResponse fromEntity(PilotProject p) {
        if (p == null) return null;
        return PilotProjectResponse.builder()
                .id(p.getId())
                .proposalId(p.getProposal() != null ? p.getProposal().getId() : null)
                .proposalTrackingNumber(p.getProposal() != null ? p.getProposal().getTrackingNumber() : null)
                .proposalTitle(p.getProposal() != null ? p.getProposal().getTitle() : null)
                .pilotCode(p.getPilotCode())
                .locationName(p.getLocationName())
                .district(p.getDistrict())
                .state(p.getState())
                .pincode(p.getPincode())
                .targetPopulation(p.getTargetPopulation() != null ? p.getTargetPopulation() : 0)
                .implementationPartnerId(p.getImplementationPartner() != null ? p.getImplementationPartner().getId() : null)
                .implementationPartnerName(p.getImplementationPartner() != null ? p.getImplementationPartner().getName() : null)
                .implementationPartnerType(p.getImplementationPartner() != null ? p.getImplementationPartner().getOrganizationType() : null)
                .startDate(p.getStartDate())
                .expectedEndDate(p.getExpectedEndDate())
                .actualEndDate(p.getActualEndDate())
                .status(p.getStatus())
                .objectives(p.getObjectives())
                .feedbackNotes(p.getFeedbackNotes())
                .communityValidationStatus(p.getCommunityValidationStatus())
                .impactMetrics(p.getImpactMetrics() != null ? p.getImpactMetrics().stream().map(ImpactMetricResponse::fromEntity).collect(Collectors.toList()) : new ArrayList<>())
                .createdById(p.getCreatedBy() != null ? p.getCreatedBy().getId() : null)
                .createdByName(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
