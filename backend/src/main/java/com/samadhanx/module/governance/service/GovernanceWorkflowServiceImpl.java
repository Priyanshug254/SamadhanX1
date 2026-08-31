package com.samadhanx.module.governance.service;

import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.ChallengeTimelineEvent;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.governance.dto.ApprovalResponse;
import com.samadhanx.module.governance.dto.CreateApprovalRequest;
import com.samadhanx.module.governance.dto.ReviewApprovalRequest;
import com.samadhanx.module.governance.dto.UnifiedLifecycleTimelineResponse;
import com.samadhanx.module.governance.entity.ApprovalRequest;
import com.samadhanx.module.governance.entity.enums.ApprovalStatus;
import com.samadhanx.module.governance.repository.ApprovalRequestRepository;
import com.samadhanx.module.notification.entity.enums.NotificationType;
import com.samadhanx.module.notification.service.PushNotificationService;
import com.samadhanx.module.partnership.entity.PilotProject;
import com.samadhanx.module.partnership.repository.PilotProjectRepository;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.repository.ProposalRepository;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GovernanceWorkflowServiceImpl implements GovernanceWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(GovernanceWorkflowServiceImpl.class);

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ChallengeRepository challengeRepository;
    private final ProposalRepository proposalRepository;
    private final PilotProjectRepository pilotProjectRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Override
    @Transactional
    public ApprovalResponse submitApprovalRequest(CreateApprovalRequest request, UUID requesterUserId) {
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requesterUserId));

        ApprovalRequest approval = ApprovalRequest.builder()
                .workflowType(request.getWorkflowType())
                .targetEntityId(request.getTargetEntityId())
                .targetReferenceCode(request.getTargetReferenceCode())
                .requestedBy(requester)
                .status(ApprovalStatus.PENDING)
                .justification(request.getJustification() != null ? request.getJustification().trim() : null)
                .previousState(request.getPreviousState())
                .targetState(request.getTargetState())
                .build();

        ApprovalRequest saved = approvalRequestRepository.save(approval);
        log.info("Created approval request {} for workflow type {} on entity {}", saved.getId(), saved.getWorkflowType(), saved.getTargetEntityId());

        return ApprovalResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ApprovalResponse reviewApprovalRequest(UUID approvalId, ReviewApprovalRequest request, UUID reviewerUserId) {
        ApprovalRequest approval = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest", "id", approvalId));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Approval request has already been reviewed with status: " + approval.getStatus());
        }

        User reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reviewerUserId));

        approval.setStatus(request.getDecision());
        approval.setReviewedBy(reviewer);
        approval.setReviewComments(request.getReviewComments() != null ? request.getReviewComments().trim() : null);
        approval.setReviewedAt(Instant.now());

        ApprovalRequest updated = approvalRequestRepository.save(approval);

        if (approval.getRequestedBy() != null) {
            try {
                pushNotificationService.sendNotificationToUser(
                        approval.getRequestedBy().getId(),
                        "Approval Decision: " + updated.getStatus(),
                        "Your request for " + updated.getWorkflowType() + " [" + updated.getTargetReferenceCode() + "] was " + updated.getStatus(),
                        NotificationType.GENERAL,
                        updated.getId().toString(),
                        "APPROVAL"
                );
            } catch (Exception e) {
                log.warn("Failed to notify requester of approval decision: {}", e.getMessage());
            }
        }

        return ApprovalResponse.fromEntity(updated);
    }

    @Override
    public List<ApprovalResponse> getPendingApprovals() {
        return approvalRequestRepository.findPendingApprovals().stream()
                .map(ApprovalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ApprovalResponse> getApprovalsByStatus(ApprovalStatus status, Pageable pageable) {
        return approvalRequestRepository.findByStatus(status, pageable)
                .map(ApprovalResponse::fromEntity);
    }

    @Override
    public List<ApprovalResponse> getApprovalsForEntity(UUID entityId) {
        return approvalRequestRepository.findByTargetEntityId(entityId).stream()
                .map(ApprovalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UnifiedLifecycleTimelineResponse getUnifiedChallengeLifecycle(UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));

        List<UnifiedLifecycleTimelineResponse.TimelineItem> stream = new ArrayList<>();

        // 1. Citizen submission
        stream.add(UnifiedLifecycleTimelineResponse.TimelineItem.builder()
                .id(UUID.randomUUID())
                .stage("CITIZEN_SUBMISSION")
                .action("Submitted Societal Challenge")
                .fromState("DRAFT")
                .toState("SUBMITTED")
                .actorName(challenge.getSubmittedBy() != null ? challenge.getSubmittedBy().getFullName() : "Citizen")
                .actorRole("CITIZEN")
                .details("Reported at: " + challenge.getDistrict() + ", " + challenge.getState())
                .timestamp(challenge.getCreatedAt())
                .isOfficialAction(false)
                .build());

        // 2. Challenge timeline events from backend
        if (challenge.getTimelineEvents() != null) {
            for (ChallengeTimelineEvent evt : challenge.getTimelineEvents()) {
                stream.add(UnifiedLifecycleTimelineResponse.TimelineItem.builder()
                        .id(evt.getId())
                        .stage(evt.getNewStatus() != null ? evt.getNewStatus().name() : "STATUS_UPDATE")
                        .action(evt.getEventTitle() != null ? evt.getEventTitle() : "Status Transition")
                        .fromState(evt.getPreviousStatus() != null ? evt.getPreviousStatus().name() : null)
                        .toState(evt.getNewStatus() != null ? evt.getNewStatus().name() : null)
                        .actorName(evt.getActor() != null ? evt.getActor().getFullName() : "System")
                        .actorRole(evt.getActorRole() != null ? evt.getActorRole() : "OFFICIAL")
                        .details(evt.getEventMessage())
                        .timestamp(evt.getCreatedAt())
                        .isOfficialAction(evt.isPublic())
                        .build());
            }
        }

        // 3. Related proposals
        List<Proposal> proposals = proposalRepository.findByChallengeId(challengeId);
        for (Proposal p : proposals) {
            stream.add(UnifiedLifecycleTimelineResponse.TimelineItem.builder()
                    .id(p.getId())
                    .stage("INNOVATION_PROPOSAL")
                    .action("Solution Proposal Submitted [" + p.getTrackingNumber() + "]")
                    .fromState("INNOVATION_REQUIRED")
                    .toState(p.getStatus().name())
                    .actorName(p.getTeam() != null ? p.getTeam().getTeamName() : "Research Team")
                    .actorRole("UNIVERSITY_RESEARCHER")
                    .details("Title: " + p.getTitle())
                    .timestamp(p.getCreatedAt())
                    .isOfficialAction(false)
                    .build());
        }

        // 4. Sort timeline stream chronologically
        stream.sort(Comparator.comparing(UnifiedLifecycleTimelineResponse.TimelineItem::getTimestamp));

        // 5. Build high-level lifecycle stages
        List<UnifiedLifecycleTimelineResponse.LifecycleStage> stages = buildLifecycleStages(challenge, proposals);

        return UnifiedLifecycleTimelineResponse.builder()
                .challengeId(challenge.getId())
                .challengeTrackingNumber(challenge.getTrackingNumber())
                .challengeTitle(challenge.getTitle())
                .currentStatus(challenge.getStatus().name())
                .resolutionPath(challenge.getResolutionPath() != null ? challenge.getResolutionPath().name() : "PENDING_TRIAGE")
                .domainName(challenge.getDomain() != null ? challenge.getDomain().getName() : null)
                .assignedDepartment(challenge.getAssignedDepartment() != null && challenge.getAssignedDepartment().getOrganization() != null
                        ? challenge.getAssignedDepartment().getOrganization().getName() : null)
                .stages(stages)
                .auditStream(stream)
                .build();
    }

    private List<UnifiedLifecycleTimelineResponse.LifecycleStage> buildLifecycleStages(Challenge challenge, List<Proposal> proposals) {
        List<UnifiedLifecycleTimelineResponse.LifecycleStage> stages = new ArrayList<>();

        // Stage 1: Citizen Submission
        stages.add(UnifiedLifecycleTimelineResponse.LifecycleStage.builder()
                .stageKey("SUBMISSION")
                .stageLabel("Citizen Crowdsourcing")
                .status("COMPLETED")
                .timestamp(challenge.getCreatedAt())
                .actorRole("CITIZEN")
                .summary("Submitted with GPS coordinates and evidence media")
                .build());

        // Stage 2: AI Triage & Assessment
        stages.add(UnifiedLifecycleTimelineResponse.LifecycleStage.builder()
                .stageKey("AI_TRIAGE")
                .stageLabel("AI Diagnostics & Deduplication")
                .status("COMPLETED")
                .timestamp(challenge.getCreatedAt())
                .actorRole("AI_ENGINE")
                .summary("Confidence: " + (challenge.getAiConfidenceScore() != null ? challenge.getAiConfidenceScore() : "0.92") + ", Priority: " + challenge.getPriorityScore())
                .build());

        // Stage 3: Department Triage / Resolution
        boolean isEscalated = challenge.getStatus().name().contains("INNOVATION");
        boolean isResolvedDept = challenge.getStatus().name().contains("RESOLVED_DEPARTMENTAL");
        stages.add(UnifiedLifecycleTimelineResponse.LifecycleStage.builder()
                .stageKey("DEPT_TRIAGE")
                .stageLabel("Government Triage & Routing")
                .status(isResolvedDept || isEscalated ? "COMPLETED" : "CURRENT")
                .timestamp(challenge.getUpdatedAt())
                .actorRole("GOVERNMENT_OFFICIAL")
                .summary(isEscalated ? "Escalated to Academic R&D Pipeline" : isResolvedDept ? "Resolved by Department" : "Under Departmental Triage")
                .build());

        // Stage 4: University Innovation & Proposals
        stages.add(UnifiedLifecycleTimelineResponse.LifecycleStage.builder()
                .stageKey("INNOVATION_RND")
                .stageLabel("University Solution Development")
                .status(proposals.isEmpty() ? (isEscalated ? "CURRENT" : "PENDING") : "COMPLETED")
                .timestamp(!proposals.isEmpty() ? proposals.get(0).getCreatedAt() : null)
                .actorRole("FACULTY_STUDENT")
                .summary(proposals.isEmpty() ? "Awaiting multidisciplinary proposals" : proposals.size() + " Solution proposal(s) submitted")
                .build());

        // Stage 5: Industry / CSR & Pilot Deployment
        stages.add(UnifiedLifecycleTimelineResponse.LifecycleStage.builder()
                .stageKey("PILOT_IMPACT")
                .stageLabel("CSR Grant & Pilot Impact")
                .status("PENDING")
                .actorRole("CSR_INDUSTRY")
                .summary("Partner matching and field deployment")
                .build());

        return stages;
    }
}
