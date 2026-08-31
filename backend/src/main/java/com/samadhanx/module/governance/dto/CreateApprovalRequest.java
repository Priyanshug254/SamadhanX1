package com.samadhanx.module.governance.dto;

import com.samadhanx.module.governance.entity.enums.WorkflowActionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApprovalRequest {

    @NotNull(message = "Workflow action type is required")
    private WorkflowActionType workflowType;

    @NotNull(message = "Target entity ID is required")
    private UUID targetEntityId;

    private String targetReferenceCode;

    private String justification;

    private String previousState;

    private String targetState;
}
