package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionalResourceRequest {

    @Schema(example = "Advanced Water Quality & Microbial Analysis Lab")
    @NotBlank(message = "Resource name is required")
    @Size(min = 2, max = 255, message = "Resource name must be between 2 and 255 characters")
    private String resourceName;

    @Schema(example = "LABORATORY")
    @NotNull(message = "Resource type is required")
    private ResourceType resourceType;

    @Schema(example = "High-precision laboratory equipped for heavy metal and contaminant detection in ground and surface water.")
    private String description;

    @Schema(example = "Atomic Absorption Spectrophotometer (AAS), Gas Chromatograph-Mass Spectrometer (GC-MS), Turbidimeters")
    private String equipmentList;

    @Schema(example = "true", description = "Can external student/faculty innovator teams book/use this facility?")
    private boolean accessibleToExternalTeams;
}
