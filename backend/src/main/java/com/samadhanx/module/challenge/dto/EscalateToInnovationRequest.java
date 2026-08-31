package com.samadhanx.module.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalateToInnovationRequest {

    @Schema(example = "The high depth salinity and localized fluoride/arsenic co-contamination cannot be remediated using standard departmental sand filters. Requires novel low-cost nanotechnology filtration membrane and remote solar distillation research from university engineering faculties.", description = "Detailed justification why novel technology, academic R&D, or startup innovation is required")
    @NotBlank(message = "Escalation justification is required")
    @Size(min = 20, max = 2000, message = "Justification must be between 20 and 2000 characters")
    private String escalationJustification;

    @Schema(example = "Low-cost graphene/clay nanomembranes, solar thermal distillation", description = "Suggested research directions / required capabilities")
    private String suggestedCapabilities;
}
