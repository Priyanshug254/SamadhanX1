package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.partnership.entity.TechTransferRecord;
import com.samadhanx.module.partnership.entity.enums.LicensingType;
import com.samadhanx.module.partnership.entity.enums.TechTransferDeploymentStatus;
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
public class TechTransferResponse {

    private UUID id;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private String assetName;
    private String ipRegistrationNumber;
    private LicensingType licensingType;
    private UUID receivingOrganizationId;
    private String receivingOrganizationName;
    private OrganizationType receivingOrganizationType;
    private Instant transferDate;
    private String responsibleParties;
    private TechTransferDeploymentStatus deploymentStatus;
    private String documentationUrl;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;

    public static TechTransferResponse fromEntity(TechTransferRecord ttr) {
        if (ttr == null) return null;
        return TechTransferResponse.builder()
                .id(ttr.getId())
                .proposalId(ttr.getProposal() != null ? ttr.getProposal().getId() : null)
                .proposalTrackingNumber(ttr.getProposal() != null ? ttr.getProposal().getTrackingNumber() : null)
                .proposalTitle(ttr.getProposal() != null ? ttr.getProposal().getTitle() : null)
                .assetName(ttr.getAssetName())
                .ipRegistrationNumber(ttr.getIpRegistrationNumber())
                .licensingType(ttr.getLicensingType())
                .receivingOrganizationId(ttr.getReceivingOrganization() != null ? ttr.getReceivingOrganization().getId() : null)
                .receivingOrganizationName(ttr.getReceivingOrganization() != null ? ttr.getReceivingOrganization().getName() : null)
                .receivingOrganizationType(ttr.getReceivingOrganization() != null ? ttr.getReceivingOrganization().getOrganizationType() : null)
                .transferDate(ttr.getTransferDate())
                .responsibleParties(ttr.getResponsibleParties())
                .deploymentStatus(ttr.getDeploymentStatus())
                .documentationUrl(ttr.getDocumentationUrl())
                .createdById(ttr.getCreatedBy() != null ? ttr.getCreatedBy().getId() : null)
                .createdByName(ttr.getCreatedBy() != null ? ttr.getCreatedBy().getFullName() : null)
                .createdAt(ttr.getCreatedAt())
                .build();
    }
}
