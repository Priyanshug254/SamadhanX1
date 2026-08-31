package com.samadhanx.module.governance.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.common.response.PageResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.governance.dto.CreateWorkItemRequest;
import com.samadhanx.module.governance.dto.RoleQueueSummaryResponse;
import com.samadhanx.module.governance.dto.UpdateWorkItemRequest;
import com.samadhanx.module.governance.dto.WorkItemResponse;
import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemStatus;
import com.samadhanx.module.governance.service.WorkItemService;
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
@RequestMapping("/api/v1/work-items")
@RequiredArgsConstructor
@Tag(name = "Work Items & Tasks", description = "Cross-ecosystem task assignment, role queues, and overdue tracking")
public class WorkItemController {

    private final WorkItemService workItemService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new work item / task")
    public ResponseEntity<ApiResponse<WorkItemResponse>> createWorkItem(
            @Valid @RequestBody CreateWorkItemRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        WorkItemResponse response = workItemService.createWorkItem(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Work item created successfully", response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update status, priority, or details of a work item")
    public ResponseEntity<ApiResponse<WorkItemResponse>> updateWorkItem(
            @PathVariable("id") UUID workItemId,
            @RequestBody UpdateWorkItemRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        WorkItemResponse response = workItemService.updateWorkItem(workItemId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Work item updated", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get work item by ID")
    public ResponseEntity<ApiResponse<WorkItemResponse>> getWorkItemById(@PathVariable("id") UUID workItemId) {
        return ResponseEntity.ok(ApiResponse.success(workItemService.getWorkItemById(workItemId)));
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active tasks assigned to the authenticated user")
    public ResponseEntity<ApiResponse<List<WorkItemResponse>>> getMyActiveTasks(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(workItemService.getMyActiveWorkItems(currentUser.getId())));
    }

    @GetMapping("/queue-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get role-specific work queue summary and metrics")
    public ResponseEntity<ApiResponse<RoleQueueSummaryResponse>> getRoleQueueSummary(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(workItemService.getRoleQueueSummary(currentUser.getId())));
    }

    @GetMapping("/challenge/{challengeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all work items related to a challenge")
    public ResponseEntity<ApiResponse<List<WorkItemResponse>>> getWorkItemsByChallenge(
            @PathVariable("challengeId") UUID challengeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(workItemService.getWorkItemsByChallenge(challengeId)));
    }

    @GetMapping("/proposal/{proposalId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all work items related to a proposal")
    public ResponseEntity<ApiResponse<List<WorkItemResponse>>> getWorkItemsByProposal(
            @PathVariable("proposalId") UUID proposalId
    ) {
        return ResponseEntity.ok(ApiResponse.success(workItemService.getWorkItemsByProposal(proposalId)));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search work items with status and priority filters")
    public ResponseEntity<ApiResponse<PageResponse<WorkItemResponse>>> searchWorkItems(
            @RequestParam(name = "status", required = false) WorkItemStatus status,
            @RequestParam(name = "priority", required = false) WorkItemPriority priority,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<WorkItemResponse> page = workItemService.searchWorkItems(status, priority, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }
}
