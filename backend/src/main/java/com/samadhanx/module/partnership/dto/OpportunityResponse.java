package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.CollaborationOpportunity;
import com.samadhanx.module.partnership.entity.enums.CollaborationType;
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
public class OpportunityResponse {

    private UUID id;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private String title;
    private String description;
    private CollaborationType collaborationType;
    private String skillsSought;
    private String requiredResources;
    private String targetSectors;
    private boolean isOpen;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;

    public static OpportunityResponse fromEntity(CollaborationOpportunity co) {
        if (co == null) return null;
        return OpportunityResponse.builder()
                .id(co.getId())
                .proposalId(co.getProposal() != null ? co.getProposal().getId() : null)
                .proposalTrackingNumber(co.getProposal() != null ? co.getProposal().getTrackingNumber() : null)
                .proposalTitle(co.getProposal() != null ? co.getProposal().getTitle() : null)
                .title(co.getTitle())
                .description(co.getDescription())
                .collaborationType(co.getCollaborationType())
                .skillsSought(co.getSkillsSought())
                .requiredResources(co.getRequiredResources())
                .targetSectors(co.getTargetSectors())
                .isOpen(co.isOpen())
                .createdById(co.getCreatedBy() != null ? co.getCreatedBy().getId() : null)
                .createdByName(co.getCreatedBy() != null ? co.getCreatedBy().getFullName() : null)
                .createdAt(co.getCreatedAt())
                .build();
    }
}
