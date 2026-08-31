package com.samadhanx.module.governance.service;

import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.governance.dto.CreateWorkItemRequest;
import com.samadhanx.module.governance.dto.RoleQueueSummaryResponse;
import com.samadhanx.module.governance.dto.UpdateWorkItemRequest;
import com.samadhanx.module.governance.dto.WorkItemResponse;
import com.samadhanx.module.governance.entity.WorkItem;
import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemStatus;
import com.samadhanx.module.governance.repository.ApprovalRequestRepository;
import com.samadhanx.module.governance.repository.WorkItemRepository;
import com.samadhanx.module.notification.entity.enums.NotificationType;
import com.samadhanx.module.notification.service.PushNotificationService;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkItemServiceImpl implements WorkItemService {

    private static final Logger log = LoggerFactory.getLogger(WorkItemServiceImpl.class);

    private final WorkItemRepository workItemRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Override
    @Transactional
    public WorkItemResponse createWorkItem(CreateWorkItemRequest request, UUID creatorUserId) {
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorUserId));

        User assignedTo = null;
        if (request.getAssignedToUserId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedToUserId()));
        }

        WorkItem item = WorkItem.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .itemType(request.getItemType())
                .status(WorkItemStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : WorkItemPriority.MEDIUM)
                .assignedTo(assignedTo)
                .creatorUser(creator)
                .challengeId(request.getChallengeId())
                .challengeTrackingNumber(request.getChallengeTrackingNumber())
                .proposalId(request.getProposalId())
                .proposalTrackingNumber(request.getProposalTrackingNumber())
                .teamId(request.getTeamId())
                .pilotId(request.getPilotId())
                .dueDate(request.getDueDate())
                .build();

        WorkItem saved = workItemRepository.save(item);

        if (assignedTo != null) {
            try {
                pushNotificationService.sendNotificationToUser(
                        assignedTo.getId(),
                        "New Task Assigned: " + saved.getTitle(),
                        "You have been assigned a new workflow task (" + saved.getItemType() + ")",
                        NotificationType.GENERAL,
                        saved.getId().toString(),
                        "WORK_ITEM"
                );
            } catch (Exception e) {
                log.warn("Failed to dispatch push notification for work item assignment: {}", e.getMessage());
            }
        }

        return WorkItemResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public WorkItemResponse updateWorkItem(UUID workItemId, UpdateWorkItemRequest request, UUID actorUserId) {
        WorkItem item = workItemRepository.findById(workItemId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkItem", "id", workItemId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            item.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
            if (request.getStatus() == WorkItemStatus.COMPLETED) {
                item.setCompletedAt(Instant.now());
            }
        }
        if (request.getPriority() != null) {
            item.setPriority(request.getPriority());
        }
        if (request.getAssignedToUserId() != null) {
            User newAssignee = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedToUserId()));
            item.setAssignedTo(newAssignee);
        }
        if (request.getDueDate() != null) {
            item.setDueDate(request.getDueDate());
        }
        if (request.getResolutionNotes() != null) {
            item.setResolutionNotes(request.getResolutionNotes().trim());
        }

        WorkItem updated = workItemRepository.save(item);
        return WorkItemResponse.fromEntity(updated);
    }

    @Override
    public WorkItemResponse getWorkItemById(UUID workItemId) {
        WorkItem item = workItemRepository.findById(workItemId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkItem", "id", workItemId));
        return WorkItemResponse.fromEntity(item);
    }

    @Override
    public List<WorkItemResponse> getMyActiveWorkItems(UUID userId) {
        return workItemRepository.findActiveByUserId(userId).stream()
                .map(WorkItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkItemResponse> getWorkItemsByChallenge(UUID challengeId) {
        return workItemRepository.findByChallengeId(challengeId).stream()
                .map(WorkItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkItemResponse> getWorkItemsByProposal(UUID proposalId) {
        return workItemRepository.findByProposalId(proposalId).stream()
                .map(WorkItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<WorkItemResponse> searchWorkItems(WorkItemStatus status, WorkItemPriority priority, Pageable pageable) {
        return workItemRepository.searchWorkItems(status, priority, pageable)
                .map(WorkItemResponse::fromEntity);
    }

    @Override
    public RoleQueueSummaryResponse getRoleQueueSummary(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<WorkItem> myActive = workItemRepository.findActiveByUserId(userId);
        long overdueCount = workItemRepository.countOverdueWorkItems(Instant.now());
        long pendingApprovalsCount = approvalRequestRepository.countPendingApprovals();

        long criticalCount = myActive.stream()
                .filter(w -> w.getPriority() == WorkItemPriority.CRITICAL || w.getPriority() == WorkItemPriority.HIGH)
                .count();

        String roleName = user.getUserRoles() != null && !user.getUserRoles().isEmpty()
                ? user.getUserRoles().iterator().next().getRole().getName().name()
                : "USER";

        return RoleQueueSummaryResponse.builder()
                .userRole(roleName)
                .myActiveTasksCount(myActive.size())
                .pendingApprovalsCount(pendingApprovalsCount)
                .overdueWorkItemsCount(overdueCount)
                .criticalActionItemsCount(criticalCount)
                .highPriorityWorkItems(myActive.stream()
                        .filter(w -> w.getPriority() == WorkItemPriority.CRITICAL || w.getPriority() == WorkItemPriority.HIGH)
                        .limit(5)
                        .map(WorkItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .pendingApprovals(approvalRequestRepository.findPendingApprovals().stream()
                        .limit(5)
                        .map(com.samadhanx.module.governance.dto.ApprovalResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<WorkItemResponse> getOverdueWorkItems() {
        return workItemRepository.findOverdueWorkItems(Instant.now()).stream()
                .map(WorkItemResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
