package com.samadhanx.module.solution.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.solution.dto.DashboardSummaryResponse;
import com.samadhanx.module.solution.service.CollaborationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Metrics", description = "Role-specific dashboard metrics for University Admins, Faculty, Students, Evaluators, and Government Admins")
public class DashboardController {

    private final CollaborationService collaborationService;

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get personalized role-specific dashboard metrics and counts")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(@AuthenticationPrincipal UserPrincipal currentUser) {
        DashboardSummaryResponse response = collaborationService.getDashboardSummary(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
