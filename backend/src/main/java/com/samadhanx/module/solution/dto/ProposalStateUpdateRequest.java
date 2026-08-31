package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.enums.ProposalStatus;
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
public class ProposalStateUpdateRequest {

    @Schema(example = "SHORTLISTED", description = "Target state: SHORTLISTED, PROTOTYPING, PILOT_READY, REJECTED")
    @NotNull(message = "Target status is required")
    private ProposalStatus targetStatus;

    @Schema(example = "Proposal selected for Phase 1 Prototype Incubation Grant of 1,85,000 INR.")
    private String notes;

    @Schema(example = "Mandatory justification if rejecting proposal")
    private String rejectionReason;
}
