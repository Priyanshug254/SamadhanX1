package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.enums.CollaborationStatus;
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
public class ReviewCollaborationRequest {

    @Schema(example = "ACCEPTED", description = "Decision: ACCEPTED, DECLINED, or CANCELLED")
    @NotNull(message = "Decision status is required")
    private CollaborationStatus decision;

    @Schema(example = "Accepted. The ceramic fabrication slots align with our prototype timeline.")
    private String reviewRemarks;
}
