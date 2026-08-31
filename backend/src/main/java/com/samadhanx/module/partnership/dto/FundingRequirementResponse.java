package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.FundingRequirement;
import com.samadhanx.module.partnership.entity.enums.FundingCategory;
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
public class FundingRequirementResponse {

    private UUID id;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private BigDecimal requestedAmountInr;
    private String purpose;
    private FundingCategory category;
    private String justification;
    private String expectedDeliverables;
    private String proposedTimeline;
    private boolean isFulfilled;
    private int offerCount;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;

    public static FundingRequirementResponse fromEntity(FundingRequirement fr) {
        if (fr == null) return null;
        return FundingRequirementResponse.builder()
                .id(fr.getId())
                .proposalId(fr.getProposal() != null ? fr.getProposal().getId() : null)
                .proposalTrackingNumber(fr.getProposal() != null ? fr.getProposal().getTrackingNumber() : null)
                .proposalTitle(fr.getProposal() != null ? fr.getProposal().getTitle() : null)
                .requestedAmountInr(fr.getRequestedAmountInr())
                .purpose(fr.getPurpose())
                .category(fr.getCategory())
                .justification(fr.getJustification())
                .expectedDeliverables(fr.getExpectedDeliverables())
                .proposedTimeline(fr.getProposedTimeline())
                .isFulfilled(fr.isFulfilled())
                .offerCount(fr.getOffers() != null ? fr.getOffers().size() : 0)
                .createdById(fr.getCreatedBy() != null ? fr.getCreatedBy().getId() : null)
                .createdByName(fr.getCreatedBy() != null ? fr.getCreatedBy().getFullName() : null)
                .createdAt(fr.getCreatedAt())
                .build();
    }
}
