package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.CollaborationOpportunity;
import com.samadhanx.module.partnership.entity.enums.CollaborationType;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class CreateOpportunityRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Proposal ID")
    @NotNull(message = "Proposal ID is required")
    private UUID proposalId;

    @Schema(example = "Call for Industrial Ceramic Sintering & Membrane Quality Validation Partner")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(example = "Seeking startup/MSME partner equipped with high-temperature kilns to co-manufacture pilot filtration candles.")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(example = "PROTOTYPING")
    @NotNull(message = "Collaboration type is required")
    private CollaborationType collaborationType;

    @Schema(example = "Ceramic engineering, kiln sintering, ISO 10500 water testing")
    private String skillsSought;

    @Schema(example = "Industrial furnace access, 500 test candle manufacturing capacity")
    private String requiredResources;

    @Schema(example = "Water & Sanitation, Cleantech, Advanced Materials")
    private String targetSectors;
}
