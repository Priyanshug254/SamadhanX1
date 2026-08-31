package com.samadhanx.module.organization.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.organization.dto.FacultyProfileRequest;
import com.samadhanx.module.organization.dto.FacultyProfileResponse;
import com.samadhanx.module.organization.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/faculty")
@RequiredArgsConstructor
@Tag(name = "Faculty & Research Expertise", description = "Endpoints for faculty profiles, research disciplines, and mentorship capabilities")
public class FacultyController {

    private final UniversityService universityService;

    @PostMapping("/profile")
    @PreAuthorize("hasRole('FACULTY') or hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN')")
    @Operation(summary = "Create or update faculty research profile")
    public ResponseEntity<ApiResponse<FacultyProfileResponse>> saveFacultyProfile(
            @Valid @RequestBody FacultyProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        FacultyProfileResponse response = universityService.createOrUpdateFacultyProfile(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Faculty profile saved successfully", response));
    }

    @GetMapping("/profile/me")
    @PreAuthorize("hasRole('FACULTY') or hasRole('SUPER_ADMIN') or hasRole('UNIVERSITY_ADMIN')")
    @Operation(summary = "Get current authenticated faculty profile")
    public ResponseEntity<ApiResponse<FacultyProfileResponse>> getMyFacultyProfile(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        FacultyProfileResponse response = universityService.getFacultyProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Faculty profile retrieved successfully", response));
    }

    @GetMapping("/{userId}")
    @SecurityRequirements
    @Operation(summary = "Get faculty profile by user ID")
    public ResponseEntity<ApiResponse<FacultyProfileResponse>> getFacultyProfileByUserId(@PathVariable UUID userId) {
        FacultyProfileResponse response = universityService.getFacultyProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok("Faculty profile retrieved successfully", response));
    }
}
