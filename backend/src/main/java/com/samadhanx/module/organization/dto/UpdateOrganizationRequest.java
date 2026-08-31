package com.samadhanx.module.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
public class UpdateOrganizationRequest {

    @Schema(example = "Indian Institute of Technology (BHU) Varanasi")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Schema(example = "Updated institute description")
    private String description;

    @Schema(example = "https://www.iitbhu.ac.in")
    private String website;

    @Schema(example = "contact@iitbhu.ac.in")
    @Email(message = "Must be a valid email address")
    private String contactEmail;

    @Schema(example = "+915422368106")
    private String contactPhone;

    @Schema(example = "Campus Address")
    private String addressLine;

    @Schema(example = "Varanasi")
    private String district;

    @Schema(example = "Uttar Pradesh")
    private String state;

    @Schema(example = "221005")
    private String pincode;

    private List<String> domainCodes;
    private String primaryDomainCode;
}
