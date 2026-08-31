package com.samadhanx.module.solution.service;

import com.samadhanx.module.solution.dto.CreateTeamRequest;
import com.samadhanx.module.solution.dto.InviteMemberRequest;
import com.samadhanx.module.solution.dto.TeamMemberDto;
import com.samadhanx.module.solution.dto.TeamResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    TeamResponse createTeam(CreateTeamRequest request, UUID creatorUserId);

    TeamResponse getTeamById(UUID teamId);

    List<TeamResponse> getTeamsForChallenge(UUID challengeId);

    Page<TeamResponse> getTeamsForUniversity(UUID universityId, Pageable pageable);

    List<TeamResponse> getMyActiveTeams(UUID userId);

    TeamMemberDto inviteMember(UUID teamId, InviteMemberRequest request, UUID actionByUserId);

    TeamMemberDto respondToInvitation(UUID teamId, boolean accept, UUID userId);

    void removeMember(UUID teamId, UUID targetUserId, UUID actionByUserId);
}
