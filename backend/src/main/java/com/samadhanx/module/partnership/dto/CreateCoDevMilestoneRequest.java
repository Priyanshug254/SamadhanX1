package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.CoDevMilestone;
import com.samadhanx.module.partnership.entity.enums.CoDevMilestoneStatus;
import com.samadhanx.module.partnership.entity.enums.LeadParty;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class CreateCoDevMilestoneRequest {

    @Schema(example = "Milestone 1: Custom Sintering Die Fabrication")
    @NotBlank(message = "Milestone name is required")
    private String milestoneName;

    @Schema(example = "INDUSTRY_PARTNER")
    @NotNull(message = "Lead party is required")
    private LeadParty leadParty;

    @Schema(example = "Design and CNC machining of multi-candle ceramic extrusion mold.")
    @NotBlank(message = "Deliverables are required")
    private String deliverables;

    @Schema(example = "2026-10-15T00:00:00Z")
    private Instant dueDate;

    @Schema(example = "PLANNED")
    @Builder.Default
    private CoDevMilestoneStatus status = CoDevMilestoneStatus.PLANNED;

    @Schema(example = "https://docs.samadhanx.org/codev/milestone_01_spec.pdf")
    private String documentationUrl;
}
