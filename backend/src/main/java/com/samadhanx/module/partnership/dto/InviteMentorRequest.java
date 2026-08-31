package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.MentorshipEngagement;
import com.samadhanx.module.partnership.entity.enums.MentorshipStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class InviteMentorRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Proposal ID")
    @NotNull(message = "Proposal ID is required")
    private UUID proposalId;

    @Schema(example = "123e4567-e89b-12d3-a456-426614174001", description = "Mentor User ID")
    @NotNull(message = "Mentor User ID is required")
    private UUID mentorUserId;

    @Schema(example = "123e4567-e89b-12d3-a456-426614174002", description = "Optional Mentor Organization ID")
    private UUID mentorOrganizationId;

    @Schema(example = "Provide technical review on ceramic membrane pore size distribution and field pilot durability.")
    private String goalsAndObjectives;

    @Schema(example = "Honored to invite you as Chief Technical Mentor for Project JalShuddhi.")
    private String invitationNotes;
}
