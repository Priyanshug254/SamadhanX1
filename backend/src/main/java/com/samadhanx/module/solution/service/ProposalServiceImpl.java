package com.samadhanx.module.solution.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.dto.ProposalDocumentDto;
import com.samadhanx.module.solution.dto.ProposalResponse;
import com.samadhanx.module.solution.dto.ProposalStateUpdateRequest;
import com.samadhanx.module.solution.dto.ProposalSummaryResponse;
import com.samadhanx.module.solution.dto.ProposalTimelineEventResponse;
import com.samadhanx.module.solution.dto.SubmitProposalRequest;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.ProposalDocument;
import com.samadhanx.module.solution.entity.ProposalTimelineEvent;
import com.samadhanx.module.solution.entity.Team;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.solution.entity.enums.TeamMemberStatus;
import com.samadhanx.module.solution.repository.ProposalDocumentRepository;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.ProposalTimelineEventRepository;
import com.samadhanx.module.solution.repository.TeamMemberRepository;
import com.samadhanx.module.solution.repository.TeamRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalServiceImpl implements ProposalService {

    private static final Logger log = LoggerFactory.getLogger(ProposalServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ProposalRepository proposalRepository;
    private final ProposalDocumentRepository proposalDocumentRepository;
    private final ProposalTimelineEventRepository timelineEventRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProposalResponse submitProposal(SubmitProposalRequest request, UUID submitterUserId) {
        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", request.getChallengeId()));

        if (challenge.getStatus() != ChallengeStatus.INNOVATION_REQUIRED &&
                challenge.getStatus() != ChallengeStatus.OPEN_FOR_ACADEMIC_PROPOSALS &&
                challenge.getStatus() != ChallengeStatus.SOLUTION_PROTOTYPING &&
                challenge.getStatus() != ChallengeStatus.FIELD_PILOT_TESTING) {
            throw new BadRequestException("Proposals can only be submitted for challenges requiring innovation (Current status: " + challenge.getStatus() + ")");
        }

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getTeamId()));

        User submitter = userRepository.findById(submitterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", submitterUserId));

        // Submitter must be an active member of the team
        boolean isActiveMember = teamMemberRepository.findByTeamIdAndUserId(team.getId(), submitterUserId)
                .map(m -> m.getStatus() == TeamMemberStatus.ACTIVE)
                .orElse(false);

        if (!isActiveMember && !submitter.hasRole(RoleName.SUPER_ADMIN)) {
            throw new ForbiddenException("You must be an active member of team '" + team.getTeamName() + "' to submit a proposal");
        }

        String trackingNumber = generateProposalTrackingNumber();

        Proposal proposal = Proposal.builder()
                .trackingNumber(trackingNumber)
                .challenge(challenge)
                .team(team)
                .hackathonId(request.getHackathonId())
                .title(request.getTitle().trim())
                .problemUnderstanding(request.getProblemUnderstanding().trim())
                .proposedSolution(request.getProposedSolution().trim())
                .innovationNovelty(request.getInnovationNovelty().trim())
                .technicalApproach(request.getTechnicalApproach().trim())
                .expectedImpact(request.getExpectedImpact().trim())
                .implementationPlan(request.getImplementationPlan().trim())
                .requiredResources(request.getRequiredResources() != null ? request.getRequiredResources().trim() : null)
                .estimatedCostInr(request.getEstimatedCostInr())
                .scalabilityPlan(request.getScalabilityPlan() != null ? request.getScalabilityPlan().trim() : null)
                .sustainabilityModel(request.getSustainabilityModel() != null ? request.getSustainabilityModel().trim() : null)
                .riskMitigation(request.getRiskMitigation() != null ? request.getRiskMitigation().trim() : null)
                .prototypeDescription(request.getPrototypeDescription() != null ? request.getPrototypeDescription().trim() : null)
                .status(ProposalStatus.PROPOSED)
                .averageScore(BigDecimal.ZERO)
                .evaluationCount(0)
                .shortlisted(false)
                .submittedBy(submitter)
                .submittedAt(Instant.now())
                .build();

        Proposal saved = proposalRepository.save(proposal);

        // Save documents
        if (request.getDocuments() != null) {
            for (ProposalDocumentDto docDto : request.getDocuments()) {
                ProposalDocument doc = ProposalDocument.builder()
                        .proposal(saved)
                        .documentType(docDto.getDocumentType())
                        .documentName(docDto.getDocumentName().trim())
                        .documentUrl(docDto.getDocumentUrl().trim())
                        .uploadedBy(submitter)
                        .createdAt(Instant.now())
                        .build();
                proposalDocumentRepository.save(doc);
                saved.addDocument(doc);
            }
        }

        // Record initial timeline event
        createProposalTimelineEvent(
                saved,
                null,
                ProposalStatus.PROPOSED,
                submitter,
                getUserPrimaryRole(submitter),
                "Proposal Submitted",
                "Solution proposal '" + saved.getTitle() + "' submitted by Team '" + team.getTeamName() + "'"
        );

        log.info("Submitted solution proposal: {} [{}] for challenge: {} by team: {}",
                saved.getTitle(), saved.getTrackingNumber(), challenge.getTrackingNumber(), team.getTeamName());

        return ProposalResponse.fromEntity(saved);
    }

    @Override
    public ProposalResponse getProposalById(UUID proposalId) {
        Proposal p = proposalRepository.findByIdWithDetails(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal", "id", proposalId));
        return ProposalResponse.fromEntity(p);
    }

    @Override
    public ProposalResponse getProposalByTrackingNumber(String trackingNumber) {
        Proposal p = proposalRepository.findByTrackingNumber(trackingNumber.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Proposal", "trackingNumber", trackingNumber));
        return ProposalResponse.fromEntity(p);
    }

    @Override
    public List<ProposalResponse> getProposalsForChallenge(UUID challengeId) {
        return proposalRepository.findByChallengeId(challengeId).stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProposalResponse> getRankedProposalsForChallenge(UUID challengeId) {
        return proposalRepository.findRankedProposalsForChallenge(challengeId).stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProposalSummaryResponse> searchProposals(
            ProposalStatus status,
            UUID challengeId,
            UUID hackathonId,
            Pageable pageable
    ) {
        Specification<Proposal> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (challengeId != null) {
                predicates.add(cb.equal(root.get("challenge").get("id"), challengeId));
            }
            if (hackathonId != null) {
                predicates.add(cb.equal(root.get("hackathonId"), hackathonId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return proposalRepository.findAll(spec, pageable).map(ProposalSummaryResponse::fromEntity);
    }

    @Override
    @Transactional
    public ProposalResponse updateProposalState(UUID proposalId, ProposalStateUpdateRequest request, UUID actionByUserId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal", "id", proposalId));

        User actor = userRepository.findById(actionByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", actionByUserId));

        validateProposalStateTransitionPermission(actor);

        ProposalStatus prevStatus = proposal.getStatus();
        ProposalStatus targetStatus = request.getTargetStatus();

        validateStateTransition(prevStatus, targetStatus);

        if (targetStatus == ProposalStatus.REJECTED && !StringUtils.hasText(request.getRejectionReason())) {
            throw new BadRequestException("Rejection reason is mandatory when rejecting a solution proposal");
        }

        proposal.setStatus(targetStatus);
        if (targetStatus == ProposalStatus.SHORTLISTED) {
            proposal.setShortlisted(true);
        } else if (targetStatus == ProposalStatus.REJECTED) {
            proposal.setRejectionReason(request.getRejectionReason().trim());
        }

        // Advance Challenge status in synchrony with proposal progression
        Challenge challenge = proposal.getChallenge();
        if (targetStatus == ProposalStatus.SHORTLISTED || targetStatus == ProposalStatus.PROTOTYPING) {
            challenge.setStatus(ChallengeStatus.SOLUTION_PROTOTYPING);
            challengeRepository.save(challenge);
        } else if (targetStatus == ProposalStatus.PILOT_READY) {
            challenge.setStatus(ChallengeStatus.FIELD_PILOT_TESTING);
            challengeRepository.save(challenge);
        }

        Proposal saved = proposalRepository.save(proposal);

        String message = StringUtils.hasText(request.getNotes()) ? request.getNotes().trim() :
                "Proposal transitioned from " + prevStatus + " to " + targetStatus;

        createProposalTimelineEvent(
                saved,
                prevStatus,
                targetStatus,
                actor,
                getUserPrimaryRole(actor),
                "Status Changed: " + targetStatus.name(),
                message
        );

        log.info("Proposal {} transitioned from {} to {} by {}", saved.getTrackingNumber(), prevStatus, targetStatus, actor.getEmail());
        return ProposalResponse.fromEntity(saved);
    }

    @Override
    public List<ProposalTimelineEventResponse> getProposalTimeline(UUID proposalId) {
        return timelineEventRepository.findByProposalIdOrderByCreatedAtAsc(proposalId).stream()
                .map(ProposalTimelineEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void validateStateTransition(ProposalStatus from, ProposalStatus to) {
        if (from == to) return;

        boolean valid = switch (from) {
            case PROPOSED -> to == ProposalStatus.UNDER_REVIEW || to == ProposalStatus.SHORTLISTED || to == ProposalStatus.REJECTED;
            case UNDER_REVIEW -> to == ProposalStatus.SHORTLISTED || to == ProposalStatus.REJECTED;
            case SHORTLISTED -> to == ProposalStatus.PROTOTYPING || to == ProposalStatus.REJECTED;
            case PROTOTYPING -> to == ProposalStatus.TESTING || to == ProposalStatus.PILOT_READY || to == ProposalStatus.REJECTED;
            case TESTING -> to == ProposalStatus.PILOT_READY || to == ProposalStatus.PROTOTYPING || to == ProposalStatus.REJECTED;
            case PILOT_READY -> to == ProposalStatus.PILOT_ACTIVE || to == ProposalStatus.DEPLOYMENT_READY || to == ProposalStatus.REJECTED;
            case PILOT_ACTIVE -> to == ProposalStatus.DEPLOYMENT_READY || to == ProposalStatus.COMPLETED || to == ProposalStatus.REJECTED;
            case DEPLOYMENT_READY -> to == ProposalStatus.DEPLOYED || to == ProposalStatus.TECHNOLOGY_TRANSFERRED || to == ProposalStatus.COMPLETED || to == ProposalStatus.REJECTED;
            case DEPLOYED -> to == ProposalStatus.TECHNOLOGY_TRANSFERRED || to == ProposalStatus.COMPLETED;
            case TECHNOLOGY_TRANSFERRED -> to == ProposalStatus.COMPLETED;
            case COMPLETED, REJECTED -> false;
        };

        if (!valid) {
            throw new BadRequestException("Invalid proposal state transition from " + from + " to " + to);
        }
    }

    private void validateProposalStateTransitionPermission(User actor) {
        if (!actor.hasRole(RoleName.SUPER_ADMIN) &&
                !actor.hasRole(RoleName.GOVERNMENT_ADMIN) &&
                !actor.hasRole(RoleName.UNIVERSITY_ADMIN)) {
            throw new ForbiddenException("Only administrators and designated jury members can update proposal progression states");
        }
    }

    private void createProposalTimelineEvent(
            Proposal proposal,
            ProposalStatus prevStatus,
            ProposalStatus newStatus,
            User actor,
            String actorRole,
            String title,
            String message
    ) {
        ProposalTimelineEvent event = ProposalTimelineEvent.builder()
                .proposal(proposal)
                .previousStatus(prevStatus)
                .newStatus(newStatus)
                .actor(actor)
                .actorRole(actorRole)
                .eventTitle(title)
                .eventMessage(message)
                .createdAt(Instant.now())
                .build();
        timelineEventRepository.save(event);
        proposal.addTimelineEvent(event);
    }

    private String generateProposalTrackingNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int rand = 10000 + RANDOM.nextInt(90000);
        return "PRP-" + datePart + "-" + rand;
    }

    private String getUserPrimaryRole(User user) {
        return user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("STUDENT");
    }
}
