package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.Team;
import com.samadhanx.module.solution.entity.enums.TeamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {

    private UUID id;
    private String teamName;
    private String description;
    private UUID challengeId;
    private String challengeTitle;
    private String challengeTrackingNumber;
    private UUID homeUniversityId;
    private String homeUniversityName;
    private UUID createdById;
    private String createdByName;
    private TeamStatus status;
    private boolean hasFacultyMentor;
    private List<TeamMemberDto> members;
    private Instant createdAt;
    private Instant updatedAt;

    public static TeamResponse fromEntity(Team t) {
        if (t == null) return null;

        String chTitle = null;
        String chTrack = null;
        UUID chId = null;
        if (t.getChallenge() != null) {
            chId = t.getChallenge().getId();
            chTitle = t.getChallenge().getTitle();
            chTrack = t.getChallenge().getTrackingNumber();
        }

        String univName = null;
        UUID univId = null;
        if (t.getHomeUniversity() != null) {
            univId = t.getHomeUniversity().getId();
            univName = t.getHomeUniversity().getName();
        }

        String cName = null;
        UUID cId = null;
        if (t.getCreatedBy() != null) {
            cId = t.getCreatedBy().getId();
            cName = t.getCreatedBy().getFullName();
        }

        List<TeamMemberDto> memberList = null;
        if (t.getMembers() != null) {
            memberList = t.getMembers().stream()
                    .map(TeamMemberDto::fromEntity)
                    .collect(Collectors.toList());
        }

        return TeamResponse.builder()
                .id(t.getId())
                .teamName(t.getTeamName())
                .description(t.getDescription())
                .challengeId(chId)
                .challengeTitle(chTitle)
                .challengeTrackingNumber(chTrack)
                .homeUniversityId(univId)
                .homeUniversityName(univName)
                .createdById(cId)
                .createdByName(cName)
                .status(t.getStatus())
                .hasFacultyMentor(t.hasFacultyMentor())
                .members(memberList)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
