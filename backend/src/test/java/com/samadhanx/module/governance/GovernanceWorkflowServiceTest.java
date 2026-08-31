package com.samadhanx.module.governance;

import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.governance.dto.ApprovalResponse;
import com.samadhanx.module.governance.dto.CreateApprovalRequest;
import com.samadhanx.module.governance.dto.ReviewApprovalRequest;
import com.samadhanx.module.governance.dto.UnifiedLifecycleTimelineResponse;
import com.samadhanx.module.governance.entity.ApprovalRequest;
import com.samadhanx.module.governance.entity.enums.ApprovalStatus;
import com.samadhanx.module.governance.entity.enums.WorkflowActionType;
import com.samadhanx.module.governance.repository.ApprovalRequestRepository;
import com.samadhanx.module.governance.service.GovernanceWorkflowServiceImpl;
import com.samadhanx.module.notification.service.PushNotificationService;
import com.samadhanx.module.partnership.repository.PilotProjectRepository;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GovernanceWorkflowServiceTest {

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private PilotProjectRepository pilotProjectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PushNotificationService pushNotificationService;

    private GovernanceWorkflowServiceImpl workflowService;

    private User requester;
    private User reviewer;
    private UUID requesterId;
    private UUID reviewerId;

    @BeforeEach
    void setUp() {
        workflowService = new GovernanceWorkflowServiceImpl(
                approvalRequestRepository,
                challengeRepository,
                proposalRepository,
                pilotProjectRepository,
                userRepository,
                pushNotificationService
        );

        requesterId = UUID.randomUUID();
        reviewerId = UUID.randomUUID();

        requester = User.builder().id(requesterId).firstName("Faculty").lastName("Lead").email("faculty@iitbhu.ac.in").build();
        reviewer = User.builder().id(reviewerId).firstName("Govt").lastName("Admin").email("admin@samadhanx.gov.in").build();
    }

    @Test
    @DisplayName("Should submit approval request with PENDING status")
    void testSubmitApprovalRequest() {
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));

        ApprovalRequest saved = ApprovalRequest.builder()
                .id(UUID.randomUUID())
                .workflowType(WorkflowActionType.CSR_SPONSORSHIP_APPROVAL)
                .targetEntityId(UUID.randomUUID())
                .targetReferenceCode("CSR-TATA-001")
                .requestedBy(requester)
                .status(ApprovalStatus.PENDING)
                .justification("Grant agreement for 50L potable water scale-up")
                .build();

        when(approvalRequestRepository.save(any())).thenReturn(saved);

        CreateApprovalRequest request = CreateApprovalRequest.builder()
                .workflowType(WorkflowActionType.CSR_SPONSORSHIP_APPROVAL)
                .targetEntityId(UUID.randomUUID())
                .targetReferenceCode("CSR-TATA-001")
                .justification("Grant agreement for 50L potable water scale-up")
                .build();

        ApprovalResponse response = workflowService.submitApprovalRequest(request, requesterId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(response.getTargetReferenceCode()).isEqualTo("CSR-TATA-001");
    }

    @Test
    @DisplayName("Should approve request and record reviewer and timestamp")
    void testReviewApprovalRequestApproved() {
        UUID approvalId = UUID.randomUUID();
        ApprovalRequest pending = ApprovalRequest.builder()
                .id(approvalId)
                .workflowType(WorkflowActionType.PILOT_DEPLOYMENT_APPROVAL)
                .status(ApprovalStatus.PENDING)
                .requestedBy(requester)
                .build();

        when(approvalRequestRepository.findById(approvalId)).thenReturn(Optional.of(pending));
        when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
        when(approvalRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewApprovalRequest request = ReviewApprovalRequest.builder()
                .decision(ApprovalStatus.APPROVED)
                .reviewComments("Verified field deployment readiness and IoT telemetry baseline.")
                .build();

        ApprovalResponse response = workflowService.reviewApprovalRequest(approvalId, request, reviewerId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(response.getReviewedByUserId()).isEqualTo(reviewerId);
        assertThat(response.getReviewedAt()).isNotNull();
        verify(pushNotificationService).sendNotificationToUser(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should reject review on already completed approval request")
    void testRejectAlreadyReviewedApproval() {
        UUID approvalId = UUID.randomUUID();
        ApprovalRequest alreadyApproved = ApprovalRequest.builder()
                .id(approvalId)
                .status(ApprovalStatus.APPROVED)
                .build();

        when(approvalRequestRepository.findById(approvalId)).thenReturn(Optional.of(alreadyApproved));

        ReviewApprovalRequest request = ReviewApprovalRequest.builder()
                .decision(ApprovalStatus.REJECTED)
                .build();

        assertThatThrownBy(() -> workflowService.reviewApprovalRequest(approvalId, request, reviewerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been reviewed");
    }

    @Test
    @DisplayName("Should build unified lifecycle timeline for challenge")
    void testUnifiedChallengeLifecycle() {
        UUID challengeId = UUID.randomUUID();
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .trackingNumber("SMX-2026-0099")
                .title("Arsenic filtration in community wells")
                .status(ChallengeStatus.INNOVATION_REQUIRED)
                .submittedBy(requester)
                .district("Chandauli")
                .state("UP")
                .latitude(new BigDecimal("25.26"))
                .longitude(new BigDecimal("83.26"))
                .build();

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(proposalRepository.findByChallengeId(challengeId)).thenReturn(Collections.emptyList());

        UnifiedLifecycleTimelineResponse timeline = workflowService.getUnifiedChallengeLifecycle(challengeId);

        assertThat(timeline).isNotNull();
        assertThat(timeline.getChallengeTrackingNumber()).isEqualTo("SMX-2026-0099");
        assertThat(timeline.getStages()).hasSize(5);
        assertThat(timeline.getAuditStream()).isNotEmpty();
        assertThat(timeline.getAuditStream().get(0).getStage()).isEqualTo("CITIZEN_SUBMISSION");
    }
}
