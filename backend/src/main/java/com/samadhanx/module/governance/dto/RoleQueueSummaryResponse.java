package com.samadhanx.module.governance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleQueueSummaryResponse {

    private String userRole;
    private long myActiveTasksCount;
    private long pendingApprovalsCount;
    private long overdueWorkItemsCount;
    private long criticalActionItemsCount;

    private List<WorkItemResponse> highPriorityWorkItems;
    private List<ApprovalResponse> pendingApprovals;
}
