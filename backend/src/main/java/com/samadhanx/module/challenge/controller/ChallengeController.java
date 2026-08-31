package com.samadhanx.module.challenge.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.challenge.dto.ChallengeResponse;
import com.samadhanx.module.challenge.dto.ChallengeSummaryResponse;
import com.samadhanx.module.challenge.dto.DepartmentActionRequest;
import com.samadhanx.module.challenge.dto.DepartmentResolveRequest;
import com.samadhanx.module.challenge.dto.EndorsementRequest;
import com.samadhanx.module.challenge.dto.EndorsementResponse;
import com.samadhanx.module.challenge.dto.EscalateToInnovationRequest;
import com.samadhanx.module.challenge.dto.SubmitChallengeRequest;
import com.samadhanx.module.challenge.dto.TimelineEventResponse;
import com.samadhanx.module.challenge.dto.UniversityChallengeMatchResponse;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.entity.enums.ResolutionPath;
import com.samadhanx.module.challenge.service.ChallengeService;
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
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
@Tag(name = "Societal Challenge Lifecycle", description = "Endpoints for crowdsourcing, AI categorization, department triage, and university innovation pipeline")
public class ChallengeController {

    private final ChallengeService challengeService;

    // ── Citizen Crowdsourcing ──────────────────────────────────

    @PostMapping
    @Operation(summary = "Submit new societal challenge", description = "Crowdsources a societal problem with GIS coordinates, priority assessment, and evidence attachments")
    public ResponseEntity<ApiResponse<ChallengeResponse>> submitChallenge(
            @Valid @RequestBody SubmitChallengeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ChallengeResponse response = challengeService.submitChallenge(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Societal challenge successfully registered and routed", response));
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Search public challenges", description = "Filter by domain, status, resolution path, state, district, or keyword with pagination")
    public ResponseEntity<ApiResponse<Page<ChallengeSummaryResponse>>> searchChallenges(
            @RequestParam(required = false) UUID domainId,
            @RequestParam(required = false) ChallengeStatus status,
            @RequestParam(required = false) ResolutionPath resolutionPath,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ChallengeSummaryResponse> results = challengeService.searchChallenges(
                domainId, status, resolutionPath, state, district, search, pageable
        );
        return ResponseEntity.ok(ApiResponse.ok("Challenges retrieved successfully", results));
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Get full challenge details by ID")
    public ResponseEntity<ApiResponse<ChallengeResponse>> getChallengeById(@PathVariable UUID id) {
        ChallengeResponse response = challengeService.getChallengeById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/tracking/{trackingNumber}")
    @SecurityRequirements
    @Operation(summary = "Track challenge by public tracking number (e.g., SMX-2026-08-98412)")
    public ResponseEntity<ApiResponse<ChallengeResponse>> getChallengeByTrackingNumber(@PathVariable String trackingNumber) {
        ChallengeResponse response = challengeService.getChallengeByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/my-submissions")
    @Operation(summary = "List challenges submitted by authenticated user")
    public ResponseEntity<ApiResponse<Page<ChallengeSummaryResponse>>> getMySubmissions(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ChallengeSummaryResponse> results = challengeService.getMySubmissions(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("Your submitted challenges", results));
    }

    @PostMapping("/{id}/endorse")
    @Operation(summary = "Endorse / Upvote challenge", description = "Validates community impact and increments challenge priority score")
    public ResponseEntity<ApiResponse<EndorsementResponse>> endorseChallenge(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) EndorsementRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        EndorsementResponse response = challengeService.endorseChallenge(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Challenge endorsed successfully", response));
    }

    @GetMapping("/{id}/timeline")
    @SecurityRequirements
    @Operation(summary = "Get public progress timeline for challenge")
    public ResponseEntity<ApiResponse<List<TimelineEventResponse>>> getTimeline(@PathVariable UUID id) {
        List<TimelineEventResponse> timeline = challengeService.getTimeline(id);
        return ResponseEntity.ok(ApiResponse.ok("Challenge timeline retrieved", timeline));
    }

    // ── Department Triage & Resolution ────────────────────────

    @GetMapping("/department/{departmentId}/assigned")
    @PreAuthorize("hasRole('GOVERNMENT_OFFICIAL') or hasRole('GOVERNMENT_ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get department assigned challenge queue")
    public ResponseEntity<ApiResponse<Page<ChallengeSummaryResponse>>> getDepartmentAssignedQueue(
            @PathVariable UUID departmentId,
            @PageableDefault(size = 20, sort = "priorityScore", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ChallengeSummaryResponse> queue = challengeService.getDepartmentAssignedQueue(departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Department queue retrieved successfully", queue));
    }

    @PostMapping("/{id}/department-action")
    @PreAuthorize("hasRole('GOVERNMENT_OFFICIAL') or hasRole('GOVERNMENT_ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Perform department triage action (Accept, Reassign, Request info, Inspection notes)")
    public ResponseEntity<ApiResponse<ChallengeResponse>> performDepartmentAction(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentActionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ChallengeResponse response = challengeService.performDepartmentAction(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Department action recorded successfully", response));
    }

    @PostMapping("/{id}/department-resolve")
    @PreAuthorize("hasRole('GOVERNMENT_OFFICIAL') or hasRole('GOVERNMENT_ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Resolve challenge via standard departmental works")
    public ResponseEntity<ApiResponse<ChallengeResponse>> resolveDepartmentalStandard(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentResolveRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ChallengeResponse response = challengeService.resolveDepartmentalStandard(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Challenge successfully marked resolved by department", response));
    }

    @PostMapping("/{id}/escalate-to-innovation")
    @PreAuthorize("hasRole('GOVERNMENT_OFFICIAL') or hasRole('GOVERNMENT_ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Escalate challenge to Academic Innovation Ecosystem (INNOVATION_REQUIRED)")
    public ResponseEntity<ApiResponse<ChallengeResponse>> escalateToInnovation(
            @PathVariable UUID id,
            @Valid @RequestBody EscalateToInnovationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ChallengeResponse response = challengeService.escalateToInnovation(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Challenge escalated to Academic Innovation Pipeline", response));
    }

    // ── Academic & University Ecosystem ───────────────────────

    @GetMapping("/innovation-pipeline")
    @SecurityRequirements
    @Operation(summary = "Browse challenges requiring novel technology / university R&D (INNOVATION_REQUIRED)")
    public ResponseEntity<ApiResponse<Page<ChallengeSummaryResponse>>> getInnovationPipeline(
            @RequestParam(required = false) UUID domainId,
            @PageableDefault(size = 20, sort = "priorityScore", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ChallengeSummaryResponse> pipeline = challengeService.getInnovationPipeline(domainId, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Academic innovation pipeline retrieved", pipeline));
    }

    @GetMapping("/matching-university/{universityOrgId}")
    @PreAuthorize("hasRole('UNIVERSITY_ADMIN') or hasRole('FACULTY') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get AI-matched open challenges for a university based on discipline, faculty expertise, and labs")
    public ResponseEntity<ApiResponse<List<UniversityChallengeMatchResponse>>> getMatchingChallengesForUniversity(
            @PathVariable UUID universityOrgId
    ) {
        List<UniversityChallengeMatchResponse> matches = challengeService.getMatchingChallengesForUniversity(universityOrgId);
        return ResponseEntity.ok(ApiResponse.ok("Matched challenges retrieved successfully", matches));
    }
}
