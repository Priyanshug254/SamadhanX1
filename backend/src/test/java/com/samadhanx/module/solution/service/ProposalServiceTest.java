package com.samadhanx.module.solution.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.dto.ProposalResponse;
import com.samadhanx.module.solution.dto.ProposalStateUpdateRequest;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.Team;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.solution.repository.ProposalDocumentRepository;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.ProposalTimelineEventRepository;
import com.samadhanx.module.solution.repository.TeamMemberRepository;
import com.samadhanx.module.solution.repository.TeamRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProposalService Unit Tests")
class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private ProposalDocumentRepository proposalDocumentRepository;

    @Mock
    private ProposalTimelineEventRepository timelineEventRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProposalServiceImpl proposalService;

    private Proposal proposal;
    private Challenge challenge;
    private User admin;

    @BeforeEach
    void setUp() {
        challenge = Challenge.builder()
                .id(UUID.randomUUID())
                .title("Rural Water Contamination")
                .status(ChallengeStatus.INNOVATION_REQUIRED)
                .build();

        Team team = Team.builder()
                .id(UUID.randomUUID())
                .teamName("EcoJal Team")
                .build();

        proposal = Proposal.builder()
                .id(UUID.randomUUID())
                .trackingNumber("PRP-2026-08-54321")
                .title("Terracotta Nanomembrane Filter")
                .challenge(challenge)
                .team(team)
                .status(ProposalStatus.UNDER_REVIEW)
                .build();

        admin = User.builder()
                .id(UUID.randomUUID())
                .email("admin@samadhanx.gov.in")
                .firstName("Super")
                .lastName("Administrator")
                .build();
        admin.addRole(Role.builder().name(RoleName.SUPER_ADMIN).build());
    }

    @Test
    @DisplayName("Should advance proposal from UNDER_REVIEW to SHORTLISTED and update challenge status")
    void shouldShortlistProposal() {
        ProposalStateUpdateRequest req = ProposalStateUpdateRequest.builder()
                .targetStatus(ProposalStatus.SHORTLISTED)
                .notes("Selected for Incubation Grant")
                .build();

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(proposalRepository.save(any(Proposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProposalResponse response = proposalService.updateProposalState(proposal.getId(), req, admin.getId());

        assertNotNull(response);
        assertEquals(ProposalStatus.SHORTLISTED, response.getStatus());
        assertTrue(response.isShortlisted());
        assertEquals(ChallengeStatus.SOLUTION_PROTOTYPING, challenge.getStatus());
        verify(challengeRepository, times(1)).save(challenge);
    }

    @Test
    @DisplayName("Should reject invalid state transition (e.g., jumping from PROPOSED to PILOT_READY directly)")
    void shouldRejectInvalidStateTransition() {
        proposal.setStatus(ProposalStatus.PROPOSED);
        ProposalStateUpdateRequest req = ProposalStateUpdateRequest.builder()
                .targetStatus(ProposalStatus.PILOT_READY)
                .build();

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThrows(BadRequestException.class, () ->
                proposalService.updateProposalState(proposal.getId(), req, admin.getId())
        );
    }
}
