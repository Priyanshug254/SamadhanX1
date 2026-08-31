package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.partnership.entity.CoDevelopmentProject;
import com.samadhanx.module.partnership.entity.enums.CoDevStatus;
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
public class CoDevProjectResponse {

    private UUID id;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private UUID partnerOrganizationId;
    private String partnerOrganizationName;
    private OrganizationType partnerOrganizationType;
    private String title;
    private String objectives;
    private String leadAcademicCoordinator;
    private String leadIndustryCoordinator;
    private Instant startDate;
    private Instant targetCompletionDate;
    private CoDevStatus status;
    
    @Builder.Default
    private List<CoDevMilestoneResponse> milestones = new ArrayList<>();
    private Instant createdAt;

    public static CoDevProjectResponse fromEntity(CoDevelopmentProject cdp) {
        if (cdp == null) return null;
        return CoDevProjectResponse.builder()
                .id(cdp.getId())
                .proposalId(cdp.getProposal() != null ? cdp.getProposal().getId() : null)
                .proposalTrackingNumber(cdp.getProposal() != null ? cdp.getProposal().getTrackingNumber() : null)
                .proposalTitle(cdp.getProposal() != null ? cdp.getProposal().getTitle() : null)
                .partnerOrganizationId(cdp.getPartnerOrganization() != null ? cdp.getPartnerOrganization().getId() : null)
                .partnerOrganizationName(cdp.getPartnerOrganization() != null ? cdp.getPartnerOrganization().getName() : null)
                .partnerOrganizationType(cdp.getPartnerOrganization() != null ? cdp.getPartnerOrganization().getOrganizationType() : null)
                .title(cdp.getTitle())
                .objectives(cdp.getObjectives())
                .leadAcademicCoordinator(cdp.getLeadAcademicCoordinator())
                .leadIndustryCoordinator(cdp.getLeadIndustryCoordinator())
                .startDate(cdp.getStartDate())
                .targetCompletionDate(cdp.getTargetCompletionDate())
                .status(cdp.getStatus())
                .milestones(cdp.getMilestones() != null ? cdp.getMilestones().stream().map(CoDevMilestoneResponse::fromEntity).collect(Collectors.toList()) : new ArrayList<>())
                .createdAt(cdp.getCreatedAt())
                .build();
    }
}
