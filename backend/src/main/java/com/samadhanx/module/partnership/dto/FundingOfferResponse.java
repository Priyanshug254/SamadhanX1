package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.partnership.entity.FundingOffer;
import com.samadhanx.module.partnership.entity.enums.FundingOfferStatus;
import com.samadhanx.module.partnership.entity.enums.FundingSupportType;
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
public class FundingOfferResponse {

    private UUID id;
    private UUID requirementId;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private UUID sponsorOrganizationId;
    private String sponsorOrganizationName;
    private OrganizationType sponsorOrganizationType;
    private BigDecimal offeredAmountInr;
    private FundingSupportType supportType;
    private FundingOfferStatus status;
    private String termsAndConditions;
    private BigDecimal disbursedAmountInr;
    private Instant disbursedAt;
    private String utilizationReport;
    private String evidenceDocumentUrl;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;

    public static FundingOfferResponse fromEntity(FundingOffer fo) {
        if (fo == null) return null;
        return FundingOfferResponse.builder()
                .id(fo.getId())
                .requirementId(fo.getRequirement() != null ? fo.getRequirement().getId() : null)
                .proposalId(fo.getProposal() != null ? fo.getProposal().getId() : null)
                .proposalTrackingNumber(fo.getProposal() != null ? fo.getProposal().getTrackingNumber() : null)
                .proposalTitle(fo.getProposal() != null ? fo.getProposal().getTitle() : null)
                .sponsorOrganizationId(fo.getSponsorOrganization() != null ? fo.getSponsorOrganization().getId() : null)
                .sponsorOrganizationName(fo.getSponsorOrganization() != null ? fo.getSponsorOrganization().getName() : null)
                .sponsorOrganizationType(fo.getSponsorOrganization() != null ? fo.getSponsorOrganization().getOrganizationType() : null)
                .offeredAmountInr(fo.getOfferedAmountInr())
                .supportType(fo.getSupportType())
                .status(fo.getStatus())
                .termsAndConditions(fo.getTermsAndConditions())
                .disbursedAmountInr(fo.getDisbursedAmountInr())
                .disbursedAt(fo.getDisbursedAt())
                .utilizationReport(fo.getUtilizationReport())
                .evidenceDocumentUrl(fo.getEvidenceDocumentUrl())
                .createdById(fo.getCreatedBy() != null ? fo.getCreatedBy().getId() : null)
                .createdByName(fo.getCreatedBy() != null ? fo.getCreatedBy().getFullName() : null)
                .createdAt(fo.getCreatedAt())
                .build();
    }
}
