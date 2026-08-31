package com.samadhanx.module.solution.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.common.response.PageResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.solution.dto.CreateHackathonRequest;
import com.samadhanx.module.solution.dto.HackathonResponse;
import com.samadhanx.module.solution.entity.enums.HackathonStatus;
import com.samadhanx.module.solution.service.HackathonService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hackathons")
@RequiredArgsConstructor
@Tag(name = "Hackathons & Innovation Competitions", description = "APIs for creating and managing problem statement hackathons and competition events")
public class HackathonController {

    private final HackathonService hackathonService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GOVERNMENT_ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Publish a new Hackathon / Problem Statement Competition")
    public ResponseEntity<ApiResponse<HackathonResponse>> createHackathon(
            @Valid @RequestBody CreateHackathonRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        HackathonResponse response = hackathonService.createHackathon(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Hackathon published successfully", response));
    }

    @GetMapping
    @Operation(summary = "List open and upcoming hackathons and innovation challenges")
    public ResponseEntity<ApiResponse<PageResponse<HackathonResponse>>> listHackathons(
            @RequestParam(required = false) HackathonStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<HackathonResponse> page = hackathonService.listHackathons(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hackathon details with included societal challenges and deadlines")
    public ResponseEntity<ApiResponse<HackathonResponse>> getHackathonById(@PathVariable UUID id) {
        HackathonResponse response = hackathonService.getHackathonById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get hackathon by its unique code (e.g. SMX-HACK-JAL-2026)")
    public ResponseEntity<ApiResponse<HackathonResponse>> getHackathonByCode(@PathVariable String code) {
        HackathonResponse response = hackathonService.getHackathonByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/evaluators/{evaluatorUserId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GOVERNMENT_ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Assign an expert evaluator to the competition jury panel")
    public ResponseEntity<ApiResponse<Void>> assignEvaluator(
            @PathVariable UUID id,
            @PathVariable UUID evaluatorUserId,
            @RequestParam(required = false, defaultValue = "Domain Expert") String domain,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        hackathonService.assignEvaluatorToHackathon(id, evaluatorUserId, domain, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Evaluator assigned to hackathon jury", null));
    }
}
