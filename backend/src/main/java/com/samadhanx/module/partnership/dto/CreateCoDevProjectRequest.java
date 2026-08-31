package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.partnership.entity.CoDevelopmentProject;
import com.samadhanx.module.partnership.entity.enums.CoDevStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateCoDevProjectRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Proposal ID")
    @NotNull(message = "Proposal ID is required")
    private UUID proposalId;

    @Schema(example = "123e4567-e89b-12d3-a456-426614174001", description = "Partner Organization ID")
    @NotNull(message = "Partner Organization ID is required")
    private UUID partnerOrganizationId;

    @Schema(example = "Joint Ceramic Extrusion & Rural Pilot Deployment Initiative")
    @NotBlank(message = "Project title is required")
    private String title;

    @Schema(example = "Collaborative development of low-cost terracotta candle molds and field deployment across 5 Gram Panchayats.")
    @NotBlank(message = "Objectives are required")
    private String objectives;

    @Schema(example = "Dr. Anil Iyer (Associate Professor, IIT BHU)")
    private String leadAcademicCoordinator;

    @Schema(example = "Vikram Malhotra (VP Engineering, CeramicTech Corp)")
    private String leadIndustryCoordinator;

    @Schema(example = "2026-12-31T00:00:00Z")
    private Instant targetCompletionDate;
}
