package com.samadhanx.module.auth.dto;

import com.samadhanx.module.role.entity.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
public class RegisterRequest {

    @Schema(example = "rajesh.kumar@example.com", description = "Valid email address for login and notifications")
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Schema(example = "Secret@12345", description = "Password must be at least 8 characters with letters, numbers, and symbols")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Schema(example = "Rajesh", description = "User's first name")
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Schema(example = "Kumar", description = "User's last name")
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Schema(example = "+919876543210", description = "Optional phone number")
    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "Phone number must be valid format (10-15 digits)")
    private String phoneNumber;

    @Schema(example = "CITIZEN", description = "Target role for registration (must be self-registerable; defaults to CITIZEN)")
    private RoleName role;
}
