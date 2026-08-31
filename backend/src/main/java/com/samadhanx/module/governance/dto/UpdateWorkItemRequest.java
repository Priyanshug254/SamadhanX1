package com.samadhanx.module.governance.dto;

import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkItemRequest {

    private String title;
    private String description;
    private WorkItemStatus status;
    private WorkItemPriority priority;
    private UUID assignedToUserId;
    private Instant dueDate;
    private String resolutionNotes;
}
