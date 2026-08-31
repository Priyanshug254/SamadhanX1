package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.ChallengeDepartmentAction;
import com.samadhanx.module.challenge.entity.enums.DepartmentActionType;
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
public class DepartmentActionResponse {

    private UUID id;
    private UUID challengeId;
    private UUID departmentId;
    private String departmentName;
    private UUID performedById;
    private String performedByName;
    private DepartmentActionType actionType;
    private String fieldInspectionNotes;
    private String actionNotes;
    private String escalationJustification;
    private Instant createdAt;

    public static DepartmentActionResponse fromEntity(ChallengeDepartmentAction cda) {
        if (cda == null) return null;

        String deptName = null;
        UUID deptId = null;
        if (cda.getDepartment() != null) {
            deptId = cda.getDepartment().getOrganizationId();
            if (cda.getDepartment().getOrganization() != null) {
                deptName = cda.getDepartment().getOrganization().getName();
            }
        }

        String pName = null;
        UUID pId = null;
        if (cda.getPerformedBy() != null) {
            pId = cda.getPerformedBy().getId();
            pName = cda.getPerformedBy().getFullName();
        }

        return DepartmentActionResponse.builder()
                .id(cda.getId())
                .challengeId(cda.getChallenge() != null ? cda.getChallenge().getId() : null)
                .departmentId(deptId)
                .departmentName(deptName)
                .performedById(pId)
                .performedByName(pName)
                .actionType(cda.getActionType())
                .fieldInspectionNotes(cda.getFieldInspectionNotes())
                .actionNotes(cda.getActionNotes())
                .escalationJustification(cda.getEscalationJustification())
                .createdAt(cda.getCreatedAt())
                .build();
    }
}
