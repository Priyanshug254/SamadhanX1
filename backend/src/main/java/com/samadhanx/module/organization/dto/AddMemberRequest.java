package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.enums.OrgMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {

    @Schema(example = "rajesh.kumar@example.com", description = "User's registered email in SamadhanX")
    @NotBlank(message = "User email is required")
    @Email(message = "Must be a valid email address")
    private String userEmail;

    @Schema(example = "FACULTY_LEAD", description = "Role within this organization")
    @NotNull(message = "Organization role is required")
    private OrgMemberRole orgRole;

    @Schema(example = "Professor & Head of Department")
    private String designation;

    @Schema(example = "EMP-94821", description = "Employee ID, Student ID, or official registration number")
    private String identifier;
}
