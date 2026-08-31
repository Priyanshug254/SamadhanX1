package com.samadhanx.module.solution.service;

import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.dto.DashboardSummaryResponse;
import com.samadhanx.module.solution.dto.DiscussionResponse;
import com.samadhanx.module.solution.dto.PostDiscussionRequest;
import com.samadhanx.module.solution.entity.ProjectDiscussion;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.Team;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.solution.repository.HackathonRepository;
import com.samadhanx.module.solution.repository.ProjectDiscussionRepository;
import com.samadhanx.module.solution.repository.ProposalEvaluationRepository;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.TeamMemberRepository;
import com.samadhanx.module.solution.repository.TeamRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollaborationServiceImpl implements CollaborationService {

    private final ProjectDiscussionRepository discussionRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalEvaluationRepository evaluationRepository;
    private final ChallengeRepository challengeRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DiscussionResponse postDiscussion(UUID teamId, PostDiscussionRequest request, UUID senderUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderUserId));

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, senderUserId) && !sender.hasRole(RoleName.SUPER_ADMIN)) {
            throw new ForbiddenException("You must be an active member of team '" + team.getTeamName() + "' to post discussion messages");
        }

        Proposal proposal = null;
        if (request.getProposalId() != null) {
            proposal = proposalRepository.findById(request.getProposalId()).orElse(null);
        }

        ProjectDiscussion discussion = ProjectDiscussion.builder()
                .team(team)
                .proposal(proposal)
                .sender(sender)
                .message(request.getMessage().trim())
                .mentorGuidance(request.isMentorGuidance())
                .attachmentUrl(request.getAttachmentUrl() != null ? request.getAttachmentUrl().trim() : null)
                .build();

        ProjectDiscussion saved = discussionRepository.save(discussion);
        return DiscussionResponse.fromEntity(saved);
    }

    @Override
    public Page<DiscussionResponse> getDiscussions(UUID teamId, Pageable pageable) {
        return discussionRepository.findByTeamId(teamId, pageable).map(DiscussionResponse::fromEntity);
    }

    @Override
    public DashboardSummaryResponse getDashboardSummary(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        long openInnovation = challengeRepository.countByStatus(ChallengeStatus.INNOVATION_REQUIRED)
                + challengeRepository.countByStatus(ChallengeStatus.OPEN_FOR_ACADEMIC_PROPOSALS);

        long totalTeams = teamRepository.count();
        long totalProposals = proposalRepository.count();
        long underReview = proposalRepository.countByStatus(ProposalStatus.UNDER_REVIEW);
        long shortlisted = proposalRepository.countByStatus(ProposalStatus.SHORTLISTED);
        long prototyping = proposalRepository.countByStatus(ProposalStatus.PROTOTYPING);
        long pilotReady = proposalRepository.countByStatus(ProposalStatus.PILOT_READY);

        long myTeams = teamRepository.findActiveTeamsForUser(userId).size();
        long myEvaluations = evaluationRepository.countByEvaluatorId(userId);
        long activeHackathons = hackathonRepository.count();

        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("CITIZEN");

        return DashboardSummaryResponse.builder()
                .role(primaryRole)
                .openInnovationChallenges(openInnovation)
                .totalTeams(totalTeams)
                .totalProposals(totalProposals)
                .underReviewProposals(underReview)
                .shortlistedProposals(shortlisted)
                .prototypingProposals(prototyping)
                .pilotReadyProposals(pilotReady)
                .myActiveProjects(myTeams)
                .myPendingEvaluations(myEvaluations)
                .activeHackathons(activeHackathons)
                .build();
    }
}
