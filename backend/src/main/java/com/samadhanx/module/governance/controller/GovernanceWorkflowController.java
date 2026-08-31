package com.samadhanx.module.governance.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.common.response.PageResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.governance.dto.ApprovalResponse;
import com.samadhanx.module.governance.dto.CreateApprovalRequest;
import com.samadhanx.module.governance.dto.ReviewApprovalRequest;
import com.samadhanx.module.governance.dto.UnifiedLifecycleTimelineResponse;
import com.samadhanx.module.governance.entity.enums.ApprovalStatus;
import com.samadhanx.module.governance.service.GovernanceWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/governance")
@RequiredArgsConstructor
@Tag(name = "Governance & Approval Workflow", description = "Multi-party approvals, state transitions, and unified audit timelines")
public class GovernanceWorkflowController {

    private final GovernanceWorkflowService governanceWorkflowService;

    @PostMapping("/approvals")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit a workflow action for official approval")
    public ResponseEntity<ApiResponse<ApprovalResponse>> submitApprovalRequest(
            @Valid @RequestBody CreateApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ApprovalResponse response = governanceWorkflowService.submitApprovalRequest(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Approval request submitted", response));
    }

    @PostMapping("/approvals/{id}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GOVERNMENT_ADMIN', 'GOVERNMENT_OFFICIAL', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Review and decide on an approval request (Approve / Reject / Changes Requested)")
    public ResponseEntity<ApiResponse<ApprovalResponse>> reviewApprovalRequest(
            @PathVariable("id") UUID approvalId,
            @Valid @RequestBody ReviewApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ApprovalResponse response = governanceWorkflowService.reviewApprovalRequest(approvalId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Approval decision recorded", response));
    }

    @GetMapping("/approvals/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all pending approval requests awaiting review")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getPendingApprovals() {
        return ResponseEntity.ok(ApiResponse.success(governanceWorkflowService.getPendingApprovals()));
    }

    @GetMapping("/approvals")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get approvals filtered by status")
    public ResponseEntity<ApiResponse<PageResponse<ApprovalResponse>>> getApprovalsByStatus(
            @RequestParam(name = "status", required = false, defaultValue = "PENDING") ApprovalStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ApprovalResponse> page = governanceWorkflowService.getApprovalsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/timeline/challenge/{challengeId}")
    @Operation(summary = "Get unified end-to-end lifecycle timeline for a challenge")
    public ResponseEntity<ApiResponse<UnifiedLifecycleTimelineResponse>> getUnifiedLifecycleTimeline(
            @PathVariable("challengeId") UUID challengeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(governanceWorkflowService.getUnifiedChallengeLifecycle(challengeId)));
    }
}
