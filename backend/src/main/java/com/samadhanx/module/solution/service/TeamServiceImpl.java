package com.samadhanx.module.solution.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.OrganizationMemberRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.dto.CreateTeamRequest;
import com.samadhanx.module.solution.dto.InviteMemberRequest;
import com.samadhanx.module.solution.dto.TeamMemberDto;
import com.samadhanx.module.solution.dto.TeamResponse;
import com.samadhanx.module.solution.entity.Team;
import com.samadhanx.module.solution.entity.TeamMember;
import com.samadhanx.module.solution.entity.enums.TeamMemberStatus;
import com.samadhanx.module.solution.entity.enums.TeamRole;
import com.samadhanx.module.solution.entity.enums.TeamStatus;
import com.samadhanx.module.solution.repository.TeamMemberRepository;
import com.samadhanx.module.solution.repository.TeamRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamServiceImpl.class);

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ChallengeRepository challengeRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, UUID creatorUserId) {
        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", request.getChallengeId()));

        if (challenge.getStatus() != ChallengeStatus.INNOVATION_REQUIRED &&
                challenge.getStatus() != ChallengeStatus.OPEN_FOR_ACADEMIC_PROPOSALS &&
                challenge.getStatus() != ChallengeStatus.SOLUTION_PROTOTYPING &&
                challenge.getStatus() != ChallengeStatus.FIELD_PILOT_TESTING) {
            throw new BadRequestException("Teams can only be formed for challenges open in the Academic Innovation Ecosystem");
        }

        Organization homeUniv = organizationRepository.findById(request.getHomeUniversityId())
                .orElseThrow(() -> new ResourceNotFoundException("Home University", "id", request.getHomeUniversityId()));

        if (homeUniv.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Home university must be a VERIFIED institution in SamadhanX");
        }

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorUserId));

        // Create Team
        Team team = Team.builder()
                .teamName(request.getTeamName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .challenge(challenge)
                .homeUniversity(homeUniv)
                .createdBy(creator)
                .status(TeamStatus.FORMING)
                .build();

        Team savedTeam = teamRepository.save(team);

        // Creator automatically added as ACTIVE TEAM_LEAD
        TeamMember creatorMember = TeamMember.builder()
                .team(savedTeam)
                .user(creator)
                .university(homeUniv)
                .teamRole(TeamRole.TEAM_LEAD)
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();
        teamMemberRepository.save(creatorMember);
        savedTeam.addMember(creatorMember);

        // Process initial members
        if (request.getInitialMembers() != null) {
            for (CreateTeamRequest.InitialMemberRequest initMember : request.getInitialMembers()) {
                if (initMember.getUserId().equals(creatorUserId)) continue;

                User targetUser = userRepository.findById(initMember.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", initMember.getUserId()));
                Organization targetUniv = organizationRepository.findById(initMember.getUniversityId())
                        .orElseThrow(() -> new ResourceNotFoundException("University", "id", initMember.getUniversityId()));

                TeamMember member = TeamMember.builder()
                        .team(savedTeam)
                        .user(targetUser)
                        .university(targetUniv)
                        .teamRole(initMember.getTeamRole())
                        .academicDiscipline(initMember.getAcademicDiscipline() != null ? initMember.getAcademicDiscipline().trim() : null)
                        .invitationNotes(initMember.getInvitationNotes() != null ? initMember.getInvitationNotes().trim() : null)
                        .status(TeamMemberStatus.INVITED)
                        .build();
                teamMemberRepository.save(member);
                savedTeam.addMember(member);
            }
        }

        log.info("Created team: '{}' for challenge: {} by user: {}", savedTeam.getTeamName(), challenge.getTrackingNumber(), creator.getEmail());
        return TeamResponse.fromEntity(savedTeam);
    }

    @Override
    public TeamResponse getTeamById(UUID teamId) {
        Team team = teamRepository.findByIdWithDetails(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        return TeamResponse.fromEntity(team);
    }

    @Override
    public List<TeamResponse> getTeamsForChallenge(UUID challengeId) {
        return teamRepository.findByChallengeId(challengeId).stream()
                .map(TeamResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TeamResponse> getTeamsForUniversity(UUID universityId, Pageable pageable) {
        return teamRepository.findByHomeUniversityId(universityId, pageable)
                .map(TeamResponse::fromEntity);
    }

    @Override
    public List<TeamResponse> getMyActiveTeams(UUID userId) {
        return teamRepository.findActiveTeamsForUser(userId).stream()
                .map(TeamResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeamMemberDto inviteMember(UUID teamId, InviteMemberRequest request, UUID actionByUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        validateTeamLeaderOrAdmin(team, actionByUserId);

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.getUserId())) {
            throw new ConflictException("User is already a member or has a pending invitation for this team");
        }

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Organization univ = organizationRepository.findById(request.getUniversityId())
                .orElseThrow(() -> new ResourceNotFoundException("University", "id", request.getUniversityId()));

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(targetUser)
                .university(univ)
                .teamRole(request.getTeamRole())
                .academicDiscipline(request.getAcademicDiscipline() != null ? request.getAcademicDiscipline().trim() : null)
                .invitationNotes(request.getInvitationNotes() != null ? request.getInvitationNotes().trim() : null)
                .status(TeamMemberStatus.INVITED)
                .build();

        TeamMember saved = teamMemberRepository.save(member);
        team.addMember(saved);

        log.info("Invited user: {} to team: {} as {}", targetUser.getEmail(), team.getTeamName(), request.getTeamRole());
        return TeamMemberDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public TeamMemberDto respondToInvitation(UUID teamId, boolean accept, UUID userId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Member Invitation", "teamId/userId", teamId));

        if (member.getStatus() != TeamMemberStatus.INVITED) {
            throw new BadRequestException("Invitation has already been responded to (Current status: " + member.getStatus() + ")");
        }

        if (accept) {
            member.setStatus(TeamMemberStatus.ACTIVE);
            member.setJoinedAt(Instant.now());

            // If team was forming and now has members, set active
            Team team = member.getTeam();
            if (team.getStatus() == TeamStatus.FORMING) {
                team.setStatus(TeamStatus.ACTIVE);
                teamRepository.save(team);
            }
        } else {
            member.setStatus(TeamMemberStatus.DECLINED);
        }

        TeamMember saved = teamMemberRepository.save(member);
        log.info("User {} responded {} to team {} invitation", userId, accept ? "ACCEPT" : "DECLINE", teamId);
        return TeamMemberDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public void removeMember(UUID teamId, UUID targetUserId, UUID actionByUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        validateTeamLeaderOrAdmin(team, actionByUserId);

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Member", "teamId/userId", teamId));

        if (member.getTeamRole() == TeamRole.TEAM_LEAD && member.getUser().getId().equals(team.getCreatedBy().getId())) {
            throw new BadRequestException("Cannot remove the founding team leader from the team");
        }

        member.setStatus(TeamMemberStatus.REMOVED);
        teamMemberRepository.save(member);
        log.info("Removed member: {} from team: {}", targetUserId, teamId);
    }

    private void validateTeamLeaderOrAdmin(Team team, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.hasRole(RoleName.SUPER_ADMIN)) {
            return;
        }

        if (team.getCreatedBy().getId().equals(userId)) {
            return;
        }

        // Check if user is university admin of the home university
        if (organizationMemberRepository.existsByOrganizationIdAndUserId(team.getHomeUniversity().getId(), userId)) {
            return;
        }

        throw new ForbiddenException("You do not have administrative authority over this project team");
    }
}
