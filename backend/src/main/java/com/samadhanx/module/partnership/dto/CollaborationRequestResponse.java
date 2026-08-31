package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.partnership.entity.CollaborationRequest;
import com.samadhanx.module.partnership.entity.enums.CollaborationStatus;
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
public class CollaborationRequestResponse {

    private UUID id;
    private UUID opportunityId;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private UUID partnerOrganizationId;
    private String partnerOrganizationName;
    private OrganizationType partnerOrganizationType;
    private boolean initiatedByPartner;
    private CollaborationType collaborationType;
    private CollaborationStatus status;
    private String message;
    private String proposedContribution;
    private String nominatedContactPerson;
    private String contactEmail;
    private String reviewRemarks;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;

    public static CollaborationRequestResponse fromEntity(CollaborationRequest cr) {
        if (cr == null) return null;
        return CollaborationRequestResponse.builder()
                .id(cr.getId())
                .opportunityId(cr.getOpportunity() != null ? cr.getOpportunity().getId() : null)
                .proposalId(cr.getProposal() != null ? cr.getProposal().getId() : null)
                .proposalTrackingNumber(cr.getProposal() != null ? cr.getProposal().getTrackingNumber() : null)
                .proposalTitle(cr.getProposal() != null ? cr.getProposal().getTitle() : null)
                .partnerOrganizationId(cr.getPartnerOrganization() != null ? cr.getPartnerOrganization().getId() : null)
                .partnerOrganizationName(cr.getPartnerOrganization() != null ? cr.getPartnerOrganization().getName() : null)
                .partnerOrganizationType(cr.getPartnerOrganization() != null ? cr.getPartnerOrganization().getOrganizationType() : null)
                .initiatedByPartner(cr.isInitiatedByPartner())
                .collaborationType(cr.getCollaborationType())
                .status(cr.getStatus())
                .message(cr.getMessage())
                .proposedContribution(cr.getProposedContribution())
                .nominatedContactPerson(cr.getNominatedContactPerson())
                .contactEmail(cr.getContactEmail())
                .reviewRemarks(cr.getReviewRemarks())
                .createdById(cr.getCreatedBy() != null ? cr.getCreatedBy().getId() : null)
                .createdByName(cr.getCreatedBy() != null ? cr.getCreatedBy().getFullName() : null)
                .createdAt(cr.getCreatedAt())
                .build();
    }
}
