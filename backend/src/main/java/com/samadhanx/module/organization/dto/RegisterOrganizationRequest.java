package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.enums.OrganizationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterOrganizationRequest {

    @Schema(example = "Indian Institute of Technology Varanasi (BHU)", description = "Official registered name")
    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Schema(example = "IIT-BHU-VARANASI", description = "Unique code or AISHE / CIN / Reg number")
    @NotBlank(message = "Organization code is required")
    @Size(min = 2, max = 100, message = "Code must be between 2 and 100 characters")
    private String code;

    @Schema(example = "UNIVERSITY", description = "Type of organization")
    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;

    @Schema(example = "Premier technical institute contributing to advanced research and societal engineering solutions.")
    private String description;

    @Schema(example = "https://www.iitbhu.ac.in")
    private String website;

    @Schema(example = "registrar@iitbhu.ac.in")
    @NotBlank(message = "Contact email is required")
    @Email(message = "Must be a valid email address")
    private String contactEmail;

    @Schema(example = "+915422368106")
    private String contactPhone;

    @Schema(example = "Banaras Hindu University Campus")
    private String addressLine;

    @Schema(example = "Varanasi")
    @NotBlank(message = "District is required")
    private String district;

    @Schema(example = "Uttar Pradesh")
    @NotBlank(message = "State is required")
    private String state;

    @Schema(example = "221005")
    private String pincode;

    @Schema(example = "[\"WATER_SANITATION\", \"CLEAN_ENERGY\", \"AGRI_TECH\"]", description = "Domain/sector codes of focus")
    private List<String> domainCodes;

    @Schema(example = "WATER_SANITATION", description = "Primary domain code")
    private String primaryDomainCode;
}
