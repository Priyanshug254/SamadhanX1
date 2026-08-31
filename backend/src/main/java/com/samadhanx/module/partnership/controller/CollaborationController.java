package com.samadhanx.module.partnership.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.partnership.dto.*;
import com.samadhanx.module.partnership.service.PartnershipCollaborationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Tag(name = "Project Collaboration, Mentorship & Funding", description = "Endpoints for project collaboration opportunities, requests, expert mentorship, CSR funding & co-development")
public class CollaborationController {

    private final PartnershipCollaborationService collaborationService;

    // ── Opportunities ─────────────────────────────────────────────
    @PostMapping("/opportunities")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create collaboration opportunity for a project/proposal")
    public ResponseEntity<ApiResponse<OpportunityResponse>> createOpportunity(
            @Valid @RequestBody CreateOpportunityRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        OpportunityResponse response = collaborationService.createOpportunity(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Collaboration opportunity created", response));
    }

    @GetMapping("/opportunities/open")
    @Operation(summary = "List all open collaboration opportunities")
    public ResponseEntity<ApiResponse<List<OpportunityResponse>>> getOpenOpportunities() {
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getOpenOpportunities()));
    }

    @GetMapping("/opportunities/proposal/{proposalId}")
    @Operation(summary = "List collaboration opportunities for a proposal")
    public ResponseEntity<ApiResponse<List<OpportunityResponse>>> getOpportunitiesForProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getOpportunitiesForProposal(proposalId)));
    }

    // ── Collaboration Requests ────────────────────────────────────
    @PostMapping("/collaborations/request")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit collaboration request / application from verified partner or team")
    public ResponseEntity<ApiResponse<CollaborationRequestResponse>> submitCollaborationRequest(
            @Valid @RequestBody SubmitCollaborationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CollaborationRequestResponse response = collaborationService.submitCollaborationRequest(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Collaboration request submitted", response));
    }

    @PostMapping("/collaborations/requests/{id}/review")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Review (Accept/Decline) collaboration request")
    public ResponseEntity<ApiResponse<CollaborationRequestResponse>> reviewCollaborationRequest(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewCollaborationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CollaborationRequestResponse response = collaborationService.reviewCollaborationRequest(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Collaboration request reviewed", response));
    }

    @GetMapping("/collaborations/proposal/{proposalId}")
    @Operation(summary = "List collaboration requests for a proposal")
    public ResponseEntity<ApiResponse<List<CollaborationRequestResponse>>> getRequestsForProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getRequestsForProposal(proposalId)));
    }

    @GetMapping("/collaborations/organization/{orgId}")
    @Operation(summary = "List collaboration requests involving a partner organization")
    public ResponseEntity<ApiResponse<List<CollaborationRequestResponse>>> getRequestsForPartner(@PathVariable UUID orgId) {
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getRequestsForPartner(orgId)));
    }

    // ── Mentorship ────────────────────────────────────────────────
    @PostMapping("/mentorships/invite")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Invite an approved expert / faculty / industry specialist as mentor")
    public ResponseEntity<ApiResponse<MentorshipEngagementResponse>> inviteMentor(
            @Valid @RequestBody InviteMentorRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MentorshipEngagementResponse response = collaborationService.inviteMentor(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Mentor invited successfully", response));
    }

    @PostMapping("/mentorships/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accept mentorship invitation")
    public ResponseEntity<ApiResponse<MentorshipEngagementResponse>> acceptMentorship(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MentorshipEngagementResponse response = collaborationService.acceptMentorship(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Mentorship accepted", response));
    }

    @PostMapping("/mentorships/{id}/decline")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Decline mentorship invitation")
    public ResponseEntity<ApiResponse<MentorshipEngagementResponse>> declineMentorship(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MentorshipEngagementResponse response = collaborationService.declineMentorship(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Mentorship declined", response));
    }

    @PostMapping("/mentorships/{id}/logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Log mentorship guidance session, reviewed milestones, and action items")
    public ResponseEntity<ApiResponse<MentorshipLogResponse>> logMentorshipActivity(
            @PathVariable UUID id,
            @Valid @RequestBody LogMentorshipActivityRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MentorshipLogResponse response = collaborationService.logMentorshipActivity(id, request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Mentorship session logged", response));
    }

    @GetMapping("/mentorships/proposal/{proposalId}")
    @Operation(summary = "List mentorship engagements for a proposal")
    public ResponseEntity<ApiResponse<List<MentorshipEngagementResponse>>> getMentorshipsForProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getMentorshipsForProposal(proposalId)));
    }

    @GetMapping("/mentorships/{id}/logs")
    @Operation(summary = "List all guidance session logs for a mentorship engagement")
    public ResponseEntity<ApiResponse<List<MentorshipLogResponse>>> getLogsForEngagement(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getLogsForEngagement(id)));
    }

    // ── Funding ───────────────────────────────────────────────────
    @PostMapping("/funding/requirements")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create funding/resource requirement for a project/proposal")
    public ResponseEntity<ApiResponse<FundingRequirementResponse>> createFundingRequirement(
            @Valid @RequestBody CreateFundingRequirementRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FundingRequirementResponse response = collaborationService.createFundingRequirement(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Funding requirement created", response));
    }

    @PostMapping("/funding/offers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit funding / resource support offer (Industry/CSR/Gov)")
    public ResponseEntity<ApiResponse<FundingOfferResponse>> submitFundingOffer(
            @Valid @RequestBody SubmitFundingOfferRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FundingOfferResponse response = collaborationService.submitFundingOffer(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Funding offer submitted", response));
    }

    @PostMapping("/funding/offers/{id}/review")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Review funding offer (APPROVED, DISBURSED, UTILIZED, CLOSED)")
    public ResponseEntity<ApiResponse<FundingOfferResponse>> reviewFundingOffer(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewFundingOfferRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FundingOfferResponse response = collaborationService.reviewFundingOffer(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Funding offer status updated", response));
    }

    @GetMapping("/funding/requirements/proposal/{proposalId}")
    @Operation(summary = "List funding requirements for a proposal")
    public ResponseEntity<ApiResponse<List<FundingRequirementResponse>>> getFundingRequirementsForProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getFundingRequirementsForProposal(proposalId)));
    }

    @GetMapping("/funding/offers/requirement/{reqId}")
    @Operation(summary = "List funding offers for a requirement")
    public ResponseEntity<ApiResponse<List<FundingOfferResponse>>> getFundingOffersForRequirement(@PathVariable UUID reqId) {
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getFundingOffersForRequirement(reqId)));
    }

    // ── Co-Development ────────────────────────────────────────────
    @PostMapping("/co-development")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Form co-development project between academic team and partner")
    public ResponseEntity<ApiResponse<CoDevProjectResponse>> createCoDevProject(
            @Valid @RequestBody CreateCoDevProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CoDevProjectResponse response = collaborationService.createCoDevProject(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Co-Development project initialized", response));
    }

    @PostMapping("/co-development/{id}/milestones")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add milestone to co-development project")
    public ResponseEntity<ApiResponse<CoDevMilestoneResponse>> addCoDevMilestone(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCoDevMilestoneRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CoDevMilestoneResponse response = collaborationService.addCoDevMilestone(id, request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Co-Development milestone added", response));
    }

    @GetMapping("/co-development/proposal/{proposalId}")
    @Operation(summary = "List co-development projects for a proposal")
    public ResponseEntity<ApiResponse<List<CoDevProjectResponse>>> getCoDevProjectsForProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getCoDevProjectsForProposal(proposalId)));
    }
}
