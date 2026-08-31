package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.PilotProject;
import com.samadhanx.module.partnership.entity.enums.CommunityValidationStatus;
import com.samadhanx.module.partnership.entity.enums.PilotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreatePilotProjectRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Proposal ID")
    @NotNull(message = "Proposal ID is required")
    private UUID proposalId;

    @Schema(example = "Chiraigaon Gram Panchayat Community Water Pilot")
    @NotBlank(message = "Location name is required")
    private String locationName;

    @Schema(example = "Varanasi")
    @NotBlank(message = "District is required")
    private String district;

    @Schema(example = "Uttar Pradesh")
    @NotBlank(message = "State is required")
    private String state;

    @Schema(example = "221112")
    private String pincode;

    @Schema(example = "3500", description = "Target population benefited")
    @Min(value = 1, message = "Target population must be at least 1")
    private int targetPopulation;

    @Schema(example = "123e4567-e89b-12d3-a456-426614174001", description = "Optional Implementation Partner Org ID")
    private UUID implementationPartnerId;

    @Schema(example = "Deploy 15 gravity-fed ceramic filtration units across 5 anganwadis and primary schools; monitor fluoride reduction for 90 days.")
    @NotBlank(message = "Objectives are required")
    private String objectives;

    @Schema(example = "2026-11-30T00:00:00Z")
    private Instant expectedEndDate;
}
