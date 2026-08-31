package com.samadhanx.module.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitVerificationRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "ID of the organization requesting verification")
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @Schema(description = "List of official verification proof documents (AISHE, DPIIT, Incorporation, Government Order)")
    @NotEmpty(message = "At least one supporting document is required for verification")
    @Valid
    private List<SupportingDocumentRequest> documents;
}
