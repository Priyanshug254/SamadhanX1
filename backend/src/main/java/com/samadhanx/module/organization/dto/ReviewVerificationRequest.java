package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVerificationRequest {

    @Schema(example = "VERIFIED", description = "Target verification decision: VERIFIED, REJECTED, or UNDER_REVIEW")
    @NotNull(message = "Decision status is required (VERIFIED, REJECTED, or UNDER_REVIEW)")
    private VerificationStatus decision;

    @Schema(example = "Official AISHE certificate and registration verified with ministry portal.")
    private String reviewerNotes;

    @Schema(example = "Invalid incorporation certificate; registration number does not match MCA records.", description = "Mandatory if decision is REJECTED")
    private String rejectionReason;
}
