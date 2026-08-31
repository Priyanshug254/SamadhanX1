package com.samadhanx.module.organization.dto;

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
public class SuspendOrganizationRequest {

    @Schema(example = "Suspended pending investigation of fraudulent university accreditation claims.")
    @NotBlank(message = "Suspension reason is mandatory")
    @Size(min = 10, max = 1000, message = "Reason must be between 10 and 1000 characters")
    private String reason;

    @Schema(example = "Reported by Department of Higher Education on 2026-08-29.")
    private String internalNotes;
}
