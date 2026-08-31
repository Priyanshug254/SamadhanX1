package com.samadhanx.module.governance.service;

import com.samadhanx.module.governance.dto.ApprovalResponse;
import com.samadhanx.module.governance.dto.CreateApprovalRequest;
import com.samadhanx.module.governance.dto.ReviewApprovalRequest;
import com.samadhanx.module.governance.dto.UnifiedLifecycleTimelineResponse;
import com.samadhanx.module.governance.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GovernanceWorkflowService {

    ApprovalResponse submitApprovalRequest(CreateApprovalRequest request, UUID requesterUserId);

    ApprovalResponse reviewApprovalRequest(UUID approvalId, ReviewApprovalRequest request, UUID reviewerUserId);

    List<ApprovalResponse> getPendingApprovals();

    Page<ApprovalResponse> getApprovalsByStatus(ApprovalStatus status, Pageable pageable);

    List<ApprovalResponse> getApprovalsForEntity(UUID entityId);

    UnifiedLifecycleTimelineResponse getUnifiedChallengeLifecycle(UUID challengeId);
}
