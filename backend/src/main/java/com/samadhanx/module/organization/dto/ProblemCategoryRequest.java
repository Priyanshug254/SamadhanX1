package com.samadhanx.module.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
public class ProblemCategoryRequest {

    @Schema(example = "Drainage & Sewerage Overflow", description = "Problem category name handled by this department")
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String categoryName;

    @Schema(example = "Blockages, structural pipeline damage, and open drain maintenance")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Schema(example = "7", description = "Standard SLA target resolution in days")
    @Min(value = 1, message = "Typical resolution days must be at least 1")
    private Integer typicalResolutionDays;
}
