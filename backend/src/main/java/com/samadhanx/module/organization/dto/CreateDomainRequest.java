package com.samadhanx.module.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDomainRequest {

    @Schema(example = "AGRI_TECH", description = "Unique code in UPPER_SNAKE_CASE")
    @NotBlank(message = "Domain code is required")
    @Pattern(regexp = "^[A-Z0-9_]{3,50}$", message = "Domain code must be 3-50 uppercase alphanumeric/underscore characters")
    private String code;

    @Schema(example = "Agriculture & Rural Tech", description = "Human-readable name")
    @NotBlank(message = "Domain name is required")
    @Size(min = 2, max = 100, message = "Domain name must be between 2 and 100 characters")
    private String name;

    @Schema(example = "Smart irrigation, post-harvest preservation, crop disease detection", description = "Scope and details")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
