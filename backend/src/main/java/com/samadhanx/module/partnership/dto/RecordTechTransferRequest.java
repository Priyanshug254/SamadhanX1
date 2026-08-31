package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.enums.LicensingType;
import com.samadhanx.module.partnership.entity.enums.TechTransferDeploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordTechTransferRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Proposal ID")
    @NotNull(message = "Proposal ID is required")
    private UUID proposalId;

    @Schema(example = "Gravity-Fed Hydroxyapatite Nanocomposite Membrane Filter IP & Manufacturing Specification")
    @NotBlank(message = "Asset name is required")
    private String assetName;

    @Schema(example = "IN-PAT-2026-99214")
    private String ipRegistrationNumber;

    @Schema(example = "NON_EXCLUSIVE")
    @NotNull(message = "Licensing type is required")
    private LicensingType licensingType;

    @Schema(example = "123e4567-e89b-12d3-a456-426614174001", description = "Receiving Organization ID")
    @NotNull(message = "Receiving Organization ID is required")
    private UUID receivingOrganizationId;

    @Schema(example = "IIT (BHU) Varanasi (Licensor), CeramicTech CleanWater Pvt Ltd (Licensee), UP Jal Nigam (Deployment Authority)")
    @NotBlank(message = "Responsible parties are required")
    private String responsibleParties;

    @Schema(example = "COMMERCIALIZED")
    @Builder.Default
    private TechTransferDeploymentStatus deploymentStatus = TechTransferDeploymentStatus.TRANSFERRED;

    @Schema(example = "https://docs.samadhanx.org/ip/tech_transfer_agreement_2026.pdf")
    private String documentationUrl;
}
