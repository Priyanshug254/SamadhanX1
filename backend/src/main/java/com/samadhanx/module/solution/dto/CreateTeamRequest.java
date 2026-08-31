package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {

    @Schema(example = "EcoJal Nanotech Research Team", description = "Team name")
    @NotBlank(message = "Team name is required")
    @Size(min = 3, max = 150, message = "Team name must be between 3 and 150 characters")
    private String teamName;

    @Schema(example = "Multidisciplinary R&D team consisting of environmental chemists, nanomaterial researchers and mechanical engineering students designing low-cost clay membranes.")
    private String description;

    @Schema(example = "Target Innovation-Required Challenge ID")
    @NotNull(message = "Challenge ID is required")
    private UUID challengeId;

    @Schema(example = "Home University Organization ID")
    @NotNull(message = "Home university ID is required")
    private UUID homeUniversityId;

    @Schema(description = "Optional initial member invitations (faculty mentor, student researchers)")
    private List<InitialMemberRequest> initialMembers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitialMemberRequest {
        @NotNull(message = "User ID is required")
        private UUID userId;

        @NotNull(message = "University ID is required")
        private UUID universityId;

        @NotNull(message = "Team role is required")
        private TeamRole teamRole;

        private String academicDiscipline;
        private String invitationNotes;
    }
}
