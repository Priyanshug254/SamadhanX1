package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.TeamMember;
import com.samadhanx.module.solution.entity.enums.TeamMemberStatus;
import com.samadhanx.module.solution.entity.enums.TeamRole;
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
public class TeamMemberDto {

    private UUID id;
    private UUID teamId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private UUID universityId;
    private String universityName;
    private TeamRole teamRole;
    private String academicDiscipline;
    private TeamMemberStatus status;
    private String invitationNotes;
    private Instant joinedAt;
    private Instant createdAt;

    public static TeamMemberDto fromEntity(TeamMember tm) {
        if (tm == null) return null;

        String uName = null;
        String uEmail = null;
        UUID uId = null;
        if (tm.getUser() != null) {
            uId = tm.getUser().getId();
            uName = tm.getUser().getFullName();
            uEmail = tm.getUser().getEmail();
        }

        String univName = null;
        UUID univId = null;
        if (tm.getUniversity() != null) {
            univId = tm.getUniversity().getId();
            univName = tm.getUniversity().getName();
        }

        return TeamMemberDto.builder()
                .id(tm.getId())
                .teamId(tm.getTeam() != null ? tm.getTeam().getId() : null)
                .userId(uId)
                .userName(uName)
                .userEmail(uEmail)
                .universityId(univId)
                .universityName(univName)
                .teamRole(tm.getTeamRole())
                .academicDiscipline(tm.getAcademicDiscipline())
                .status(tm.getStatus())
                .invitationNotes(tm.getInvitationNotes())
                .joinedAt(tm.getJoinedAt())
                .createdAt(tm.getCreatedAt())
                .build();
    }
}
