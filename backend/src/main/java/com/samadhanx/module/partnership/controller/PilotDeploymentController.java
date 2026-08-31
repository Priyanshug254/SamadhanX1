package com.samadhanx.module.partnership.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.partnership.dto.*;
import com.samadhanx.module.partnership.service.PilotDeploymentService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Pilot Deployment, Testing, Impact & Government Oversight", description = "Endpoints for lab validation tests, field pilot projects, measurable social KPI metrics, technology transfer, and high-level government oversight")
public class PilotDeploymentController {

    private final PilotDeploymentService pilotDeploymentService;

    // ── Validation Testing ───────────────────────────────────────
    @PostMapping("/pilots/validation-tests")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit accredited laboratory or field validation test result")
    public ResponseEntity<ApiResponse<ValidationTestResponse>> submitValidationTest(
            @Valid @RequestBody SubmitValidationTestRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ValidationTestResponse response = pilotDeploymentService.submitValidationTest(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Validation test recorded", response));
    }

    @GetMapping("/pilots/validation-tests/proposal/{proposalId}")
    @Operation(summary = "List validation tests for a proposal")
    public ResponseEntity<ApiResponse<List<ValidationTestResponse>>> getValidationTestsForProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(pilotDeploymentService.getValidationTestsForProposal(proposalId)));
    }

    // ── Pilot Projects ───────────────────────────────────────────
    @PostMapping("/pilots")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Establish new field pilot project for a solution proposal")
    public ResponseEntity<ApiResponse<PilotProjectResponse>> createPilotProject(
            @Valid @RequestBody CreatePilotProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PilotProjectResponse response = pilotDeploymentService.createPilotProject(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Pilot project created", response));
    }

    @PostMapping("/pilots/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update pilot status (PLANNED, ACTIVE, PAUSED, COMPLETED, FAILED) and community feedback")
    public ResponseEntity<ApiResponse<PilotProjectResponse>> updatePilotStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePilotStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PilotProjectResponse response = pilotDeploymentService.updatePilotStatus(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Pilot status updated", response));
    }

    @GetMapping("/pilots/{id}")
    @Operation(summary = "Get pilot project details and recorded impact metrics")
    public ResponseEntity<ApiResponse<PilotProjectResponse>> getPilotById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(pilotDeploymentService.getPilotById(id)));
    }

    @GetMapping("/pilots/proposal/{proposalId}")
    @Operation(summary = "List all pilot deployments for a proposal")
    public ResponseEntity<ApiResponse<List<PilotProjectResponse>>> getPilotsForProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(pilotDeploymentService.getPilotsForProposal(proposalId)));
    }

    // ── Impact Measurement ───────────────────────────────────────
    @PostMapping("/pilots/impact-metrics")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record measurable social impact KPI outcome")
    public ResponseEntity<ApiResponse<ImpactMetricResponse>> recordImpactMetric(
            @Valid @RequestBody RecordImpactMetricRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ImpactMetricResponse response = pilotDeploymentService.recordImpactMetric(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Impact metric recorded", response));
    }

    @PostMapping("/pilots/impact-metrics/{id}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GOVERNMENT_ADMIN', 'GOVERNMENT_OFFICIAL')")
    @Operation(summary = "Government audit verification of social impact metric (Officials/Admins only)")
    public ResponseEntity<ApiResponse<ImpactMetricResponse>> verifyImpactMetric(
            @PathVariable UUID id,
            @Valid @RequestBody VerifyImpactMetricRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ImpactMetricResponse response = pilotDeploymentService.verifyImpactMetric(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Impact metric verified by government authority", response));
    }

    @GetMapping("/pilots/impact-metrics/proposal/{proposalId}")
    @Operation(summary = "List all impact KPI measurements for a proposal")
    public ResponseEntity<ApiResponse<List<ImpactMetricResponse>>> getImpactMetricsForProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(pilotDeploymentService.getImpactMetricsForProposal(proposalId)));
    }

    @GetMapping("/pilots/impact-summary/proposal/{proposalId}")
    @Operation(summary = "Get aggregated impact summary for a completed proposal/project")
    public ResponseEntity<ApiResponse<ProjectImpactSummaryResponse>> getProjectImpactSummary(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(pilotDeploymentService.getProjectImpactSummary(proposalId)));
    }

    // ── Technology Transfer ──────────────────────────────────────
    @PostMapping("/pilots/tech-transfer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record IP licensing and Technology Transfer to industry/government receiving organization")
    public ResponseEntity<ApiResponse<TechTransferResponse>> recordTechTransfer(
            @Valid @RequestBody RecordTechTransferRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TechTransferResponse response = pilotDeploymentService.recordTechTransfer(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Technology transfer recorded", response));
    }

    @GetMapping("/pilots/tech-transfer/proposal/{proposalId}")
    @Operation(summary = "List technology transfer records for a proposal")
    public ResponseEntity<ApiResponse<List<TechTransferResponse>>> getTechTransfersForProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(ApiResponse.success(pilotDeploymentService.getTechTransfersForProposal(proposalId)));
    }

    // ── Government Oversight Dashboard ───────────────────────────
    @GetMapping("/government/oversight")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GOVERNMENT_ADMIN', 'GOVERNMENT_OFFICIAL')")
    @Operation(summary = "High-level societal challenge, academic R&D, industry collaboration, and impact oversight dashboard")
    public ResponseEntity<ApiResponse<GovernmentOversightDashboardResponse>> getGovernmentOversightDashboard() {
        GovernmentOversightDashboardResponse response = pilotDeploymentService.getGovernmentOversightDashboard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
