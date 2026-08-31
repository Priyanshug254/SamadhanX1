package com.samadhanx.module.solution.dto;

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
public class RespondInvitationRequest {

    @Schema(example = "true", description = "true to accept invitation, false to decline")
    @NotNull(message = "Accept flag is required")
    private Boolean accept;
}
