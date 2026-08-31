package com.samadhanx.module.solution.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.common.response.PageResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.solution.dto.EvaluateProposalRequest;
import com.samadhanx.module.solution.dto.ProposalEvaluationResponse;
import com.samadhanx.module.solution.dto.ProposalResponse;
import com.samadhanx.module.solution.dto.ProposalStateUpdateRequest;
import com.samadhanx.module.solution.dto.ProposalSummaryResponse;
import com.samadhanx.module.solution.dto.ProposalTimelineEventResponse;
import com.samadhanx.module.solution.dto.SubmitProposalRequest;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.solution.service.ProposalEvaluationService;
import com.samadhanx.module.solution.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/proposals")
@RequiredArgsConstructor
@Tag(name = "Solution Proposals & Evaluations", description = "APIs for submitting, evaluating, shortlisting, and tracking R&D solution proposals")
public class ProposalController {

    private final ProposalService proposalService;
    private final ProposalEvaluationService evaluationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit a multidisciplinary solution proposal for an Innovation-Required challenge")
    public ResponseEntity<ApiResponse<ProposalResponse>> submitProposal(
            @Valid @RequestBody SubmitProposalRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProposalResponse response = proposalService.submitProposal(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Solution proposal submitted successfully", response));
    }

    @GetMapping
    @Operation(summary = "Search and filter solution proposals")
    public ResponseEntity<ApiResponse<PageResponse<ProposalSummaryResponse>>> searchProposals(
            @RequestParam(required = false) ProposalStatus status,
            @RequestParam(required = false) UUID challengeId,
            @RequestParam(required = false) UUID hackathonId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProposalSummaryResponse> page = proposalService.searchProposals(status, challengeId, hackathonId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full proposal details, technical specifications, and evaluations")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposalById(@PathVariable UUID id) {
        ProposalResponse response = proposalService.getProposalById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tracking/{trackingNumber}")
    @Operation(summary = "Track a solution proposal by its human-readable tracking number (e.g. PRP-2026-08-12345)")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposalByTrackingNumber(@PathVariable String trackingNumber) {
        ProposalResponse response = proposalService.getProposalByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/challenge/{challengeId}")
    @Operation(summary = "List all solution proposals submitted for a specific challenge")
    public ResponseEntity<ApiResponse<List<ProposalResponse>>> getProposalsForChallenge(@PathVariable UUID challengeId) {
        List<ProposalResponse> list = proposalService.getProposalsForChallenge(challengeId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/challenge/{challengeId}/ranked")
    @Operation(summary = "Get ranked leaderboard of proposals for a challenge by evaluation score")
    public ResponseEntity<ApiResponse<List<ProposalResponse>>> getRankedProposals(@PathVariable UUID challengeId) {
        List<ProposalResponse> list = proposalService.getRankedProposalsForChallenge(challengeId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{id}/state")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GOVERNMENT_ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Advance proposal progression state (SHORTLISTED, PROTOTYPING, PILOT_READY, REJECTED)")
    public ResponseEntity<ApiResponse<ProposalResponse>> updateProposalState(
            @PathVariable UUID id,
            @Valid @RequestBody ProposalStateUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProposalResponse response = proposalService.updateProposalState(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Proposal state updated successfully", response));
    }

    @PostMapping("/{id}/evaluate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit expert evaluation scorecard with multi-dimensional scores (0-100)")
    public ResponseEntity<ApiResponse<ProposalEvaluationResponse>> evaluateProposal(
            @PathVariable UUID id,
            @Valid @RequestBody EvaluateProposalRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProposalEvaluationResponse response = evaluationService.evaluateProposal(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Evaluation scorecard recorded successfully", response));
    }

    @GetMapping("/{id}/evaluations")
    @Operation(summary = "Get all expert evaluations and score breakdown for a proposal")
    public ResponseEntity<ApiResponse<List<ProposalEvaluationResponse>>> getEvaluations(@PathVariable UUID id) {
        List<ProposalEvaluationResponse> list = evaluationService.getEvaluationsForProposal(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get complete proposal milestone audit and progression timeline")
    public ResponseEntity<ApiResponse<List<ProposalTimelineEventResponse>>> getTimeline(@PathVariable UUID id) {
        List<ProposalTimelineEventResponse> list = proposalService.getProposalTimeline(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
