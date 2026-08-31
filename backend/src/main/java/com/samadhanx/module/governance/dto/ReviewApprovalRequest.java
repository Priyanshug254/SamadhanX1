package com.samadhanx.module.governance.dto;

import com.samadhanx.module.governance.entity.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewApprovalRequest {

    @NotNull(message = "Decision status is required")
    private ApprovalStatus decision; // APPROVED, REJECTED, CHANGES_REQUESTED

    private String reviewComments;
}
