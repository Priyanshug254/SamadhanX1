package com.samadhanx.module.organization.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.organization.dto.AddMemberRequest;
import com.samadhanx.module.organization.dto.DepartmentProfileRequest;
import com.samadhanx.module.organization.dto.DepartmentProfileResponse;
import com.samadhanx.module.organization.dto.FacultyProfileResponse;
import com.samadhanx.module.organization.dto.IndustryProfileRequest;
import com.samadhanx.module.organization.dto.IndustryProfileResponse;
import com.samadhanx.module.organization.dto.InstitutionalResourceRequest;
import com.samadhanx.module.organization.dto.InstitutionalResourceResponse;
import com.samadhanx.module.organization.dto.OrganizationMemberResponse;
import com.samadhanx.module.organization.dto.OrganizationResponse;
import com.samadhanx.module.organization.dto.ProblemCategoryRequest;
import com.samadhanx.module.organization.dto.ProblemCategoryResponse;
import com.samadhanx.module.organization.dto.RegisterOrganizationRequest;
import com.samadhanx.module.organization.dto.UniversityProfileRequest;
import com.samadhanx.module.organization.dto.UniversityProfileResponse;
import com.samadhanx.module.organization.dto.UpdateOrganizationRequest;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.service.DepartmentService;
import com.samadhanx.module.organization.service.IndustryService;
import com.samadhanx.module.organization.service.OrganizationService;
import com.samadhanx.module.organization.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "Endpoints for onboarding and managing Government, University, and Industry organizations")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final DepartmentService departmentService;
    private final UniversityService universityService;
    private final IndustryService industryService;

    // ── Core Organization Endpoints ───────────────────────────

    @PostMapping
    @Operation(summary = "Register new organization", description = "Onboards a new institution into PENDING_VERIFICATION state")
    public ResponseEntity<ApiResponse<OrganizationResponse>> registerOrganization(
            @Valid @RequestBody RegisterOrganizationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        OrganizationResponse response = organizationService.registerOrganization(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Organization successfully registered", response));
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Search organizations", description = "Filter by type, verification status, state, district with pagination")
    public ResponseEntity<ApiResponse<Page<OrganizationResponse>>> searchOrganizations(
            @RequestParam(required = false) OrganizationType type,
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<OrganizationResponse> results = organizationService.searchOrganizations(type, status, state, district, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Organizations retrieved successfully", results));
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable UUID id) {
        OrganizationResponse response = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/code/{code}")
    @SecurityRequirements
    @Operation(summary = "Get organization by unique code")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationByCode(@PathVariable String code) {
        OrganizationResponse response = organizationService.getOrganizationByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization base profile")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        OrganizationResponse response = organizationService.updateOrganization(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Organization updated successfully", response));
    }

    @GetMapping("/my-organizations")
    @Operation(summary = "Get organizations affiliated with the current authenticated user")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getMyOrganizations(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<OrganizationResponse> orgs = organizationService.getOrganizationsForUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("User organizations retrieved successfully", orgs));
    }

    // ── Organization Membership ───────────────────────────────

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to organization")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        OrganizationMemberResponse response = organizationService.addMember(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Member added successfully", response));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List organization members")
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> getMembers(@PathVariable UUID id) {
        List<OrganizationMemberResponse> members = organizationService.getOrganizationMembers(id);
        return ResponseEntity.ok(ApiResponse.ok("Organization members retrieved successfully", members));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove member from organization")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        organizationService.removeMember(id, userId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Member removed successfully", null));
    }

    // ── Government Department Subsystem ────────────────────────

    @PutMapping("/{id}/department-profile")
    @Operation(summary = "Create or update government department profile")
    public ResponseEntity<ApiResponse<DepartmentProfileResponse>> updateDepartmentProfile(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        DepartmentProfileResponse response = departmentService.createOrUpdateDepartmentProfile(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Department profile saved successfully", response));
    }

    @GetMapping("/{id}/department-profile")
    @SecurityRequirements
    @Operation(summary = "Get government department profile")
    public ResponseEntity<ApiResponse<DepartmentProfileResponse>> getDepartmentProfile(@PathVariable UUID id) {
        DepartmentProfileResponse response = departmentService.getDepartmentProfile(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/problem-categories")
    @Operation(summary = "Add problem/service category handled by department")
    public ResponseEntity<ApiResponse<ProblemCategoryResponse>> addProblemCategory(
            @PathVariable UUID id,
            @Valid @RequestBody ProblemCategoryRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProblemCategoryResponse response = departmentService.addProblemCategory(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Problem category added successfully", response));
    }

    @GetMapping("/{id}/problem-categories")
    @SecurityRequirements
    @Operation(summary = "List problem categories handled by department")
    public ResponseEntity<ApiResponse<List<ProblemCategoryResponse>>> getProblemCategories(@PathVariable UUID id) {
        List<ProblemCategoryResponse> categories = departmentService.getProblemCategories(id);
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @DeleteMapping("/{id}/problem-categories/{categoryId}")
    @Operation(summary = "Remove problem category from department")
    public ResponseEntity<ApiResponse<Void>> removeProblemCategory(
            @PathVariable UUID id,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        departmentService.removeProblemCategory(id, categoryId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Problem category removed successfully", null));
    }

    // ── Higher Education & Research Subsystem ─────────────────

    @PutMapping("/{id}/university-profile")
    @Operation(summary = "Create or update university HEI profile")
    public ResponseEntity<ApiResponse<UniversityProfileResponse>> updateUniversityProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UniversityProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UniversityProfileResponse response = universityService.createOrUpdateUniversityProfile(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("University profile saved successfully", response));
    }

    @GetMapping("/{id}/university-profile")
    @SecurityRequirements
    @Operation(summary = "Get university HEI profile")
    public ResponseEntity<ApiResponse<UniversityProfileResponse>> getUniversityProfile(@PathVariable UUID id) {
        UniversityProfileResponse response = universityService.getUniversityProfile(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/resources")
    @Operation(summary = "Add laboratory / institutional resource")
    public ResponseEntity<ApiResponse<InstitutionalResourceResponse>> addResource(
            @PathVariable UUID id,
            @Valid @RequestBody InstitutionalResourceRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        InstitutionalResourceResponse response = universityService.addResource(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Institutional resource added successfully", response));
    }

    @GetMapping("/{id}/resources")
    @SecurityRequirements
    @Operation(summary = "List laboratories and facilities for university/research lab")
    public ResponseEntity<ApiResponse<List<InstitutionalResourceResponse>>> getResources(@PathVariable UUID id) {
        List<InstitutionalResourceResponse> resources = universityService.getResources(id);
        return ResponseEntity.ok(ApiResponse.ok(resources));
    }

    @DeleteMapping("/{id}/resources/{resourceId}")
    @Operation(summary = "Delete institutional resource")
    public ResponseEntity<ApiResponse<Void>> removeResource(
            @PathVariable UUID id,
            @PathVariable UUID resourceId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        universityService.removeResource(id, resourceId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Resource deleted successfully", null));
    }

    @GetMapping("/{id}/faculty")
    @SecurityRequirements
    @Operation(summary = "List faculty experts affiliated with university")
    public ResponseEntity<ApiResponse<List<FacultyProfileResponse>>> getFacultyMembers(@PathVariable UUID id) {
        List<FacultyProfileResponse> faculty = universityService.getFacultyProfilesForUniversity(id);
        return ResponseEntity.ok(ApiResponse.ok(faculty));
    }

    // ── Industry & Partner Subsystem ──────────────────────────

    @PutMapping("/{id}/industry-profile")
    @Operation(summary = "Create or update industry / startup / CSR profile")
    public ResponseEntity<ApiResponse<IndustryProfileResponse>> updateIndustryProfile(
            @PathVariable UUID id,
            @Valid @RequestBody IndustryProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        IndustryProfileResponse response = industryService.createOrUpdateIndustryProfile(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Industry profile saved successfully", response));
    }

    @GetMapping("/{id}/industry-profile")
    @SecurityRequirements
    @Operation(summary = "Get industry / startup / CSR profile")
    public ResponseEntity<ApiResponse<IndustryProfileResponse>> getIndustryProfile(@PathVariable UUID id) {
        IndustryProfileResponse response = industryService.getIndustryProfile(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
