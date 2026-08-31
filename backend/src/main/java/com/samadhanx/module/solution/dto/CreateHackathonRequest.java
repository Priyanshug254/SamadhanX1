package com.samadhanx.module.solution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHackathonRequest {

    @Schema(example = "National Jal Samadhan Hackathon 2026", description = "Competition / Hackathon Title")
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @Schema(example = "SMX-HACK-JAL-2026", description = "Unique code")
    @NotBlank(message = "Code is required")
    private String code;

    @Schema(example = "Nationwide societal challenge hackathon targeting rural groundwater purification, greywater recycling, and flood prediction.")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(example = "https://media.samadhanx.org/banners/jal_hackathon_2026.jpg")
    private String bannerUrl;

    @Schema(example = "Organizer University / Government Organization ID")
    @NotNull(message = "Organizer organization ID is required")
    private UUID organizerOrgId;

    @Schema(example = "Domain ID (optional theme filter)")
    private UUID domainId;

    @Schema(example = "2026-10-31T18:30:00Z", description = "Deadline for teams to submit solution proposals")
    @NotNull(message = "Submission deadline is required")
    private Instant submissionDeadline;

    @Schema(example = "2026-11-15T18:30:00Z", description = "Deadline for jury evaluations and shortlisting")
    @NotNull(message = "Evaluation deadline is required")
    private Instant evaluationDeadline;

    @Schema(description = "List of Innovation-Required Challenge IDs included in this competition")
    private List<UUID> challengeIds;

    @Schema(description = "List of evaluator User IDs assigned to the jury panel")
    private List<UUID> evaluatorUserIds;
}
