package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.DepartmentProblemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemCategoryResponse {
    private UUID id;
    private String categoryName;
    private String description;
    private Integer typicalResolutionDays;

    public static ProblemCategoryResponse fromEntity(DepartmentProblemCategory dpc) {
        if (dpc == null) return null;
        return ProblemCategoryResponse.builder()
                .id(dpc.getId())
                .categoryName(dpc.getCategoryName())
                .description(dpc.getDescription())
                .typicalResolutionDays(dpc.getTypicalResolutionDays())
                .build();
    }
}
