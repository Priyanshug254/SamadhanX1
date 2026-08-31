package com.samadhanx.module.solution.service;

import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.solution.dto.EvaluateProposalRequest;
import com.samadhanx.module.solution.dto.ProposalEvaluationResponse;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.ProposalEvaluation;
import com.samadhanx.module.solution.entity.Team;
import com.samadhanx.module.solution.entity.enums.EvaluationRecommendation;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.solution.repository.ProposalEvaluationRepository;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.ProposalTimelineEventRepository;
import com.samadhanx.module.solution.repository.TeamMemberRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProposalEvaluationService Unit Tests")
class ProposalEvaluationServiceTest {

    @Mock
    private ProposalEvaluationRepository evaluationRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private ProposalTimelineEventRepository timelineEventRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProposalEvaluationServiceImpl evaluationService;

    private Proposal proposal;
    private User submitter;
    private User externalEvaluator;
    private Team team;

    @BeforeEach
    void setUp() {
        submitter = User.builder().id(UUID.randomUUID()).email("submitter@iitbhu.ac.in").firstName("Rahul").lastName("Verma").build();
        externalEvaluator = User.builder().id(UUID.randomUUID()).email("expert.evaluator@dst.gov.in").firstName("Dr. Meera").lastName("Iyer").build();

        team = Team.builder().id(UUID.randomUUID()).teamName("EcoJal Team").build();

        proposal = Proposal.builder()
                .id(UUID.randomUUID())
                .trackingNumber("PRP-2026-08-11111")
                .title("Terracotta Nanomembrane Arsenic Filter")
                .team(team)
                .submittedBy(submitter)
                .status(ProposalStatus.PROPOSED)
                .build();
    }

    @Test
    @DisplayName("Should calculate multi-dimensional weighted score with transparent explainability")
    void shouldEvaluateProposalWithTransparentScore() {
        EvaluateProposalRequest req = EvaluateProposalRequest.builder()
                .problemUnderstandingScore(90) // 10% -> 9.0
                .innovationScore(95)           // 20% -> 19.0
                .technicalFeasibilityScore(85) // 20% -> 17.0
                .socialImpactScore(92)         // 15% -> 13.8
                .scalabilityScore(88)          // 10% -> 8.8
                .costEffectivenessScore(90)    // 10% -> 9.0
                .sustainabilityScore(85)       // 5%  -> 4.25
                .implementationReadinessScore(85)// 10% -> 8.5
                .recommendation(EvaluationRecommendation.SHORTLIST)
                .strengths("Excellent low-cost clay approach")
                .weaknesses("Pore clogging under high turbidity")
                .build();

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(userRepository.findById(externalEvaluator.getId())).thenReturn(Optional.of(externalEvaluator));
        when(teamMemberRepository.existsByTeamIdAndUserId(team.getId(), externalEvaluator.getId())).thenReturn(false);
        when(evaluationRepository.existsByProposalIdAndEvaluatorId(proposal.getId(), externalEvaluator.getId())).thenReturn(false);
        when(evaluationRepository.save(any(ProposalEvaluation.class))).thenAnswer(invocation -> {
            ProposalEvaluation pe = invocation.getArgument(0);
            pe.setId(UUID.randomUUID());
            return pe;
        });
        when(evaluationRepository.findByProposalId(proposal.getId())).thenReturn(List.of());

        ProposalEvaluationResponse response = evaluationService.evaluateProposal(proposal.getId(), req, externalEvaluator.getId());

        assertNotNull(response);
        // Sum = 9.0 + 19.0 + 17.0 + 13.8 + 8.8 + 9.0 + 4.25 + 8.5 = 89.35
        assertEquals(new BigDecimal("89.35"), response.getTotalScore());
        assertEquals(EvaluationRecommendation.SHORTLIST, response.getRecommendation());
        assertTrue(response.getScoringRationale().contains("Why this proposal scored 89.35/100"));
        assertEquals(ProposalStatus.UNDER_REVIEW, proposal.getStatus());
    }

    @Test
    @DisplayName("Should prevent submitter or team members from evaluating own proposal (Conflict of Interest)")
    void shouldPreventConflictOfInterest() {
        EvaluateProposalRequest req = EvaluateProposalRequest.builder()
                .problemUnderstandingScore(90)
                .innovationScore(90)
                .technicalFeasibilityScore(90)
                .socialImpactScore(90)
                .scalabilityScore(90)
                .costEffectivenessScore(90)
                .sustainabilityScore(90)
                .implementationReadinessScore(90)
                .recommendation(EvaluationRecommendation.SHORTLIST)
                .build();

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(userRepository.findById(submitter.getId())).thenReturn(Optional.of(submitter));

        assertThrows(ForbiddenException.class, () ->
                evaluationService.evaluateProposal(proposal.getId(), req, submitter.getId())
        );
    }
}
