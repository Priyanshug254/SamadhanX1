package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.enums.DepartmentActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentActionRequest {

    @Schema(example = "ACCEPTED_FOR_RESOLUTION", description = "Triage action type")
    @NotNull(message = "Action type is required")
    private DepartmentActionType actionType;

    @Schema(example = "Junior Engineer S.K. Mishra visited the village on Aug 29. Arsenic level verified at 0.082 mg/L.", description = "Official field inspection notes")
    private String fieldInspectionNotes;

    @Schema(example = "Assigned to Water Works Maintenance division. Pipeline extension estimated at 12 working days.", description = "Action notes / instructions")
    private String actionNotes;

    @Schema(example = "For reassignments, ID of the target department")
    private UUID reassignedDepartmentId;
}
