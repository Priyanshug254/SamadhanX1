package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.enums.CommunityValidationStatus;
import com.samadhanx.module.partnership.entity.enums.PilotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePilotStatusRequest {

    @Schema(example = "COMPLETED", description = "New pilot status: PLANNED, ACTIVE, PAUSED, COMPLETED, FAILED")
    @NotNull(message = "Status is required")
    private PilotStatus status;

    @Schema(example = "VALIDATED", description = "Community validation: PENDING, VALIDATED, CONCERNS_RAISED")
    private CommunityValidationStatus communityValidationStatus;

    @Schema(example = "Gram Pradhan and Community Health Officer issued formal certificate of clean water satisfaction.")
    private String feedbackNotes;

    @Schema(example = "2026-11-20T00:00:00Z")
    private Instant actualEndDate;
}
