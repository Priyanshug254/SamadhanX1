package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class InviteMemberRequest {

    @Schema(example = "Target User ID to invite")
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Schema(example = "University Organization ID of the invited member")
    @NotNull(message = "University ID is required")
    private UUID universityId;

    @Schema(example = "FACULTY_MENTOR", description = "TEAM_LEAD, FACULTY_MENTOR, STUDENT, RESEARCHER")
    @NotNull(message = "Team role is required")
    private TeamRole teamRole;

    @Schema(example = "Nanomaterial Chemistry")
    private String academicDiscipline;

    @Schema(example = "Invited as Lead Faculty Mentor for prototype phase")
    private String invitationNotes;
}
