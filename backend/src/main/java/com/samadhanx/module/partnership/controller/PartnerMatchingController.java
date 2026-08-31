package com.samadhanx.module.partnership.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.partnership.dto.PartnerCapabilityRequest;
import com.samadhanx.module.partnership.dto.PartnerCapabilityResponse;
import com.samadhanx.module.partnership.dto.PartnerMatchResponse;
import com.samadhanx.module.partnership.service.PartnerMatchingService;
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
@Tag(name = "Partner Capabilities & Matching", description = "Endpoints for Industry, Startup, MSME, CSR capabilities and smart explainable matching")
public class PartnerMatchingController {

    private final PartnerMatchingService partnerMatchingService;

    @PostMapping("/capabilities")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Register or update partner organization capabilities (Verified partners only)")
    public ResponseEntity<ApiResponse<PartnerCapabilityResponse>> registerCapabilities(
            @Valid @RequestBody PartnerCapabilityRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PartnerCapabilityResponse response = partnerMatchingService.registerOrUpdatePartnerCapability(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Partner capability registered successfully", response));
    }

    @GetMapping("/capabilities/{organizationId}")
    @Operation(summary = "Get partner capabilities by organization ID")
    public ResponseEntity<ApiResponse<PartnerCapabilityResponse>> getCapabilities(@PathVariable UUID organizationId) {
        PartnerCapabilityResponse response = partnerMatchingService.getPartnerCapability(organizationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/matching/proposal/{proposalId}")
    @Operation(summary = "Find smart matching verified partners (Industry/Startup/CSR/MSME) with transparent explainability")
    public ResponseEntity<ApiResponse<List<PartnerMatchResponse>>> matchPartnersForProposal(@PathVariable UUID proposalId) {
        List<PartnerMatchResponse> matches = partnerMatchingService.findMatchingPartnersForProposal(proposalId);
        return ResponseEntity.ok(ApiResponse.success(matches));
    }
}
