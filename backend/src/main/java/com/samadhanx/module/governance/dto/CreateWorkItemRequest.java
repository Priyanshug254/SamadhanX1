package com.samadhanx.module.governance.dto;

import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateWorkItemRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Work item type is required")
    private WorkItemType itemType;

    private WorkItemPriority priority;

    private UUID assignedToUserId;

    private UUID challengeId;

    private String challengeTrackingNumber;

    private UUID proposalId;

    private String proposalTrackingNumber;

    private UUID teamId;

    private UUID pilotId;

    private Instant dueDate;
}
