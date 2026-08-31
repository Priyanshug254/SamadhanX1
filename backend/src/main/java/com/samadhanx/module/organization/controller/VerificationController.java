package com.samadhanx.module.organization.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.organization.dto.OrganizationResponse;
import com.samadhanx.module.organization.dto.ReviewVerificationRequest;
import com.samadhanx.module.organization.dto.SubmitVerificationRequest;
import com.samadhanx.module.organization.dto.SuspendOrganizationRequest;
import com.samadhanx.module.organization.dto.VerificationAuditLogResponse;
import com.samadhanx.module.organization.dto.VerificationRequestResponse;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verifications")
@RequiredArgsConstructor
@Tag(name = "Organization Verification", description = "Endpoints for institutional onboarding verification workflow and audit logs")
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping
    @Operation(summary = "Submit organization verification request", description = "Uploads supporting proof documents and places organization into PENDING_VERIFICATION queue")
    public ResponseEntity<ApiResponse<VerificationRequestResponse>> submitVerification(
            @Valid @RequestBody SubmitVerificationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        VerificationRequestResponse response = verificationService.submitVerificationRequest(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Verification application submitted successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GOVERNMENT_ADMIN')")
    @Operation(summary = "Get verification queue", description = "Restricted to Super Admins and Government Admins")
    public ResponseEntity<ApiResponse<Page<VerificationRequestResponse>>> getVerificationQueue(
            @RequestParam(required = false) VerificationStatus status,
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<VerificationRequestResponse> queue = verificationService.getVerificationQueue(status, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Verification queue retrieved successfully", queue));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get verification request details by ID")
    public ResponseEntity<ApiResponse<VerificationRequestResponse>> getVerificationRequest(@PathVariable UUID id) {
        VerificationRequestResponse response = verificationService.getVerificationRequestById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/assign/{reviewerId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GOVERNMENT_ADMIN')")
    @Operation(summary = "Assign reviewer to verification request", description = "Moves status to UNDER_REVIEW")
    public ResponseEntity<ApiResponse<VerificationRequestResponse>> assignReviewer(
            @PathVariable UUID id,
            @PathVariable UUID reviewerId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        VerificationRequestResponse response = verificationService.assignReviewer(id, reviewerId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Reviewer assigned successfully", response));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('GOVERNMENT_ADMIN')")
    @Operation(summary = "Review and decide verification application", description = "Approve (VERIFIED), Reject (REJECTED), or mark UNDER_REVIEW with notes")
    public ResponseEntity<ApiResponse<VerificationRequestResponse>> reviewVerification(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewVerificationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        VerificationRequestResponse response = verificationService.reviewVerificationRequest(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Verification review decision recorded successfully", response));
    }

    @PostMapping("/organization/{orgId}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Suspend verified organization", description = "Restricted to Super Admin")
    public ResponseEntity<ApiResponse<OrganizationResponse>> suspendOrganization(
            @PathVariable UUID orgId,
            @Valid @RequestBody SuspendOrganizationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        OrganizationResponse response = verificationService.suspendOrganization(orgId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Organization successfully suspended", response));
    }

    @GetMapping("/organization/{orgId}/audit-logs")
    @Operation(summary = "Get full verification audit history for an organization", description = "Chronological log of who approved/rejected/suspended, when, and reasons")
    public ResponseEntity<ApiResponse<List<VerificationAuditLogResponse>>> getAuditLogs(@PathVariable UUID orgId) {
        List<VerificationAuditLogResponse> logs = verificationService.getAuditLogsForOrganization(orgId);
        return ResponseEntity.ok(ApiResponse.ok("Verification audit logs retrieved successfully", logs));
    }
}
