package com.samadhanx.module.solution.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.solution.dto.EvaluateProposalRequest;
import com.samadhanx.module.solution.dto.ProposalEvaluationResponse;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.ProposalEvaluation;
import com.samadhanx.module.solution.entity.ProposalTimelineEvent;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.solution.repository.ProposalEvaluationRepository;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.ProposalTimelineEventRepository;
import com.samadhanx.module.solution.repository.TeamMemberRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalEvaluationServiceImpl implements ProposalEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(ProposalEvaluationServiceImpl.class);

    // Dimension weights
    private static final double WEIGHT_PROBLEM_UNDERSTANDING = 0.10;
    private static final double WEIGHT_INNOVATION = 0.20;
    private static final double WEIGHT_TECHNICAL_FEASIBILITY = 0.20;
    private static final double WEIGHT_SOCIAL_IMPACT = 0.15;
    private static final double WEIGHT_SCALABILITY = 0.10;
    private static final double WEIGHT_COST_EFFECTIVENESS = 0.10;
    private static final double WEIGHT_SUSTAINABILITY = 0.05;
    private static final double WEIGHT_IMPLEMENTATION_READINESS = 0.10;

    private final ProposalEvaluationRepository evaluationRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalTimelineEventRepository timelineEventRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProposalEvaluationResponse evaluateProposal(UUID proposalId, EvaluateProposalRequest request, UUID evaluatorUserId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal", "id", proposalId));

        if (proposal.getStatus() != ProposalStatus.PROPOSED &&
                proposal.getStatus() != ProposalStatus.UNDER_REVIEW &&
                proposal.getStatus() != ProposalStatus.SHORTLISTED) {
            throw new BadRequestException("Proposal is currently not open for evaluation (Current status: " + proposal.getStatus() + ")");
        }

        User evaluator = userRepository.findById(evaluatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", evaluatorUserId));

        // 1. Conflict of Interest Check
        if (proposal.getSubmittedBy().getId().equals(evaluatorUserId)) {
            throw new ForbiddenException("Conflict of Interest: Evaluators cannot evaluate proposals they personally submitted");
        }

        if (teamMemberRepository.existsByTeamIdAndUserId(proposal.getTeam().getId(), evaluatorUserId)) {
            throw new ForbiddenException("Conflict of Interest: Evaluators cannot evaluate proposals from their own project team");
        }

        if (evaluationRepository.existsByProposalIdAndEvaluatorId(proposalId, evaluatorUserId)) {
            throw new ConflictException("You have already submitted an evaluation for this proposal");
        }

        // 2. Compute Weighted Composite Score
        BigDecimal totalScore = computeWeightedTotalScore(request);
        String rationale = buildScoringRationale(request, totalScore);

        ProposalEvaluation evaluation = ProposalEvaluation.builder()
                .proposal(proposal)
                .evaluator(evaluator)
                .problemUnderstandingScore(request.getProblemUnderstandingScore())
                .innovationScore(request.getInnovationScore())
                .technicalFeasibilityScore(request.getTechnicalFeasibilityScore())
                .socialImpactScore(request.getSocialImpactScore())
                .scalabilityScore(request.getScalabilityScore())
                .costEffectivenessScore(request.getCostEffectivenessScore())
                .sustainabilityScore(request.getSustainabilityScore())
                .implementationReadinessScore(request.getImplementationReadinessScore())
                .totalScore(totalScore)
                .strengths(request.getStrengths() != null ? request.getStrengths().trim() : null)
                .weaknesses(request.getWeaknesses() != null ? request.getWeaknesses().trim() : null)
                .qualitativeFeedback(request.getQualitativeFeedback() != null ? request.getQualitativeFeedback().trim() : null)
                .recommendation(request.getRecommendation())
                .scoringRationale(rationale)
                .build();

        ProposalEvaluation savedEval = evaluationRepository.save(evaluation);
        proposal.addEvaluation(savedEval);

        // 3. Update proposal average score
        List<ProposalEvaluation> allEvals = evaluationRepository.findByProposalId(proposalId);
        double sum = allEvals.stream().mapToDouble(e -> e.getTotalScore().doubleValue()).sum();
        double avg = allEvals.isEmpty() ? 0.0 : sum / allEvals.size();
        proposal.setAverageScore(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));

        // Advance state to UNDER_REVIEW if it was PROPOSED
        if (proposal.getStatus() == ProposalStatus.PROPOSED) {
            proposal.setStatus(ProposalStatus.UNDER_REVIEW);
        }

        proposalRepository.save(proposal);

        // Record timeline event
        ProposalTimelineEvent event = ProposalTimelineEvent.builder()
                .proposal(proposal)
                .previousStatus(ProposalStatus.PROPOSED)
                .newStatus(proposal.getStatus())
                .actor(evaluator)
                .actorRole("EXPERT_EVALUATOR")
                .eventTitle("Expert Evaluation Recorded")
                .eventMessage("Evaluator " + evaluator.getFullName() + " scored this proposal " + totalScore + "/100 (" + request.getRecommendation() + ")")
                .createdAt(Instant.now())
                .build();
        timelineEventRepository.save(event);

        log.info("Recorded evaluation for proposal: {} by evaluator: {}. Total score: {}",
                proposal.getTrackingNumber(), evaluator.getEmail(), totalScore);

        return ProposalEvaluationResponse.fromEntity(savedEval);
    }

    @Override
    public List<ProposalEvaluationResponse> getEvaluationsForProposal(UUID proposalId) {
        return evaluationRepository.findByProposalIdWithEvaluator(proposalId).stream()
                .map(ProposalEvaluationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private BigDecimal computeWeightedTotalScore(EvaluateProposalRequest r) {
        double score = (r.getProblemUnderstandingScore() * WEIGHT_PROBLEM_UNDERSTANDING)
                + (r.getInnovationScore() * WEIGHT_INNOVATION)
                + (r.getTechnicalFeasibilityScore() * WEIGHT_TECHNICAL_FEASIBILITY)
                + (r.getSocialImpactScore() * WEIGHT_SOCIAL_IMPACT)
                + (r.getScalabilityScore() * WEIGHT_SCALABILITY)
                + (r.getCostEffectivenessScore() * WEIGHT_COST_EFFECTIVENESS)
                + (r.getSustainabilityScore() * WEIGHT_SUSTAINABILITY)
                + (r.getImplementationReadinessScore() * WEIGHT_IMPLEMENTATION_READINESS);

        score = Math.max(0.0, Math.min(100.0, score));
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildScoringRationale(EvaluateProposalRequest r, BigDecimal totalScore) {
        return String.format(
                "Why this proposal scored %s/100: Innovation [%d/100 * 20%% = %.1f] + Feasibility [%d/100 * 20%% = %.1f] + Social Impact [%d/100 * 15%% = %.1f] + Problem Understanding [%d/100 * 10%% = %.1f] + Scalability [%d/100 * 10%% = %.1f] + Cost [%d/100 * 10%% = %.1f] + Readiness [%d/100 * 10%% = %.1f] + Sustainability [%d/100 * 5%% = %.1f]",
                totalScore.toString(),
                r.getInnovationScore(), r.getInnovationScore() * WEIGHT_INNOVATION,
                r.getTechnicalFeasibilityScore(), r.getTechnicalFeasibilityScore() * WEIGHT_TECHNICAL_FEASIBILITY,
                r.getSocialImpactScore(), r.getSocialImpactScore() * WEIGHT_SOCIAL_IMPACT,
                r.getProblemUnderstandingScore(), r.getProblemUnderstandingScore() * WEIGHT_PROBLEM_UNDERSTANDING,
                r.getScalabilityScore(), r.getScalabilityScore() * WEIGHT_SCALABILITY,
                r.getCostEffectivenessScore(), r.getCostEffectivenessScore() * WEIGHT_COST_EFFECTIVENESS,
                r.getImplementationReadinessScore(), r.getImplementationReadinessScore() * WEIGHT_IMPLEMENTATION_READINESS,
                r.getSustainabilityScore(), r.getSustainabilityScore() * WEIGHT_SUSTAINABILITY
        );
    }
}
