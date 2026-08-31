package com.samadhanx.module.governance.service;

import com.samadhanx.module.governance.dto.CreateWorkItemRequest;
import com.samadhanx.module.governance.dto.RoleQueueSummaryResponse;
import com.samadhanx.module.governance.dto.UpdateWorkItemRequest;
import com.samadhanx.module.governance.dto.WorkItemResponse;
import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface WorkItemService {

    WorkItemResponse createWorkItem(CreateWorkItemRequest request, UUID creatorUserId);

    WorkItemResponse updateWorkItem(UUID workItemId, UpdateWorkItemRequest request, UUID actorUserId);

    WorkItemResponse getWorkItemById(UUID workItemId);

    List<WorkItemResponse> getMyActiveWorkItems(UUID userId);

    List<WorkItemResponse> getWorkItemsByChallenge(UUID challengeId);

    List<WorkItemResponse> getWorkItemsByProposal(UUID proposalId);

    Page<WorkItemResponse> searchWorkItems(WorkItemStatus status, WorkItemPriority priority, Pageable pageable);

    RoleQueueSummaryResponse getRoleQueueSummary(UUID userId);

    List<WorkItemResponse> getOverdueWorkItems();
}
