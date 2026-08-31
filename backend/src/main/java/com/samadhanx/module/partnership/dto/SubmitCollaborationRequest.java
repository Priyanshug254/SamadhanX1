package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.enums.CollaborationType;
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
public class SubmitCollaborationRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Target Proposal ID")
    @NotNull(message = "Proposal ID is required")
    private UUID proposalId;

    @Schema(example = "123e4567-e89b-12d3-a456-426614174001", description = "Optional Opportunity ID")
    private UUID opportunityId;

    @Schema(example = "123e4567-e89b-12d3-a456-426614174002", description = "Partner Organization ID")
    @NotNull(message = "Partner Organization ID is required")
    private UUID partnerOrganizationId;

    @Schema(example = "PROTOTYPING")
    @NotNull(message = "Collaboration type is required")
    private CollaborationType collaborationType;

    @Schema(example = "We offer pilot ceramic extrusion and kiln firing access at our Varanasi industrial cluster facility.")
    @NotBlank(message = "Message is required")
    private String message;

    @Schema(example = "500 sintering kiln hours, technical oversight by lead materials engineer.")
    private String proposedContribution;

    @Schema(example = "Vikram Malhotra")
    private String nominatedContactPerson;

    @Schema(example = "vikram@ceramictech.co.in")
    private String contactEmail;
}
