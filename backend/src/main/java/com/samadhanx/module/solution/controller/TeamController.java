package com.samadhanx.module.solution.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.common.response.PageResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.solution.dto.CreateTeamRequest;
import com.samadhanx.module.solution.dto.DiscussionResponse;
import com.samadhanx.module.solution.dto.InviteMemberRequest;
import com.samadhanx.module.solution.dto.PostDiscussionRequest;
import com.samadhanx.module.solution.dto.RespondInvitationRequest;
import com.samadhanx.module.solution.dto.TeamMemberDto;
import com.samadhanx.module.solution.dto.TeamResponse;
import com.samadhanx.module.solution.service.CollaborationService;
import com.samadhanx.module.solution.service.TeamService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Multidisciplinary Teams", description = "APIs for forming and managing multidisciplinary project teams across universities")
public class TeamController {

    private final TeamService teamService;
    private final CollaborationService collaborationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a multidisciplinary project team for an Innovation-Required challenge")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TeamResponse response = teamService.createTeam(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Project team created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team profile with active members and mentor details")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable UUID id) {
        TeamResponse response = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/challenge/{challengeId}")
    @Operation(summary = "List all multidisciplinary teams working on a challenge")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeamsForChallenge(@PathVariable UUID challengeId) {
        List<TeamResponse> list = teamService.getTeamsForChallenge(challengeId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/university/{universityId}")
    @Operation(summary = "List teams originating from a university")
    public ResponseEntity<ApiResponse<PageResponse<TeamResponse>>> getTeamsForUniversity(
            @PathVariable UUID universityId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<TeamResponse> page = teamService.getTeamsForUniversity(universityId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/my-teams")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active teams where authenticated user is a lead, mentor, student or researcher")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getMyTeams(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<TeamResponse> list = teamService.getMyActiveTeams(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{id}/members/invite")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Invite a student, faculty mentor, or researcher to the team")
    public ResponseEntity<ApiResponse<TeamMemberDto>> inviteMember(
            @PathVariable UUID id,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TeamMemberDto response = teamService.inviteMember(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Member invitation sent successfully", response));
    }

    @PostMapping("/{id}/invitation/respond")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accept or decline a team invitation")
    public ResponseEntity<ApiResponse<TeamMemberDto>> respondToInvitation(
            @PathVariable UUID id,
            @Valid @RequestBody RespondInvitationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TeamMemberDto response = teamService.respondToInvitation(id, request.getAccept(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Responded to team invitation", response));
    }

    @DeleteMapping("/{id}/members/{targetUserId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove a member from the team")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        teamService.removeMember(id, targetUserId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Member removed from team", null));
    }

    @PostMapping("/{id}/discussions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Post a discussion message, mentor note, or project update")
    public ResponseEntity<ApiResponse<DiscussionResponse>> postDiscussion(
            @PathVariable UUID id,
            @Valid @RequestBody PostDiscussionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        DiscussionResponse response = collaborationService.postDiscussion(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Discussion message posted", response));
    }

    @GetMapping("/{id}/discussions")
    @Operation(summary = "Get team discussion thread")
    public ResponseEntity<ApiResponse<PageResponse<DiscussionResponse>>> getDiscussions(
            @PathVariable UUID id,
            @PageableDefault(size = 30) Pageable pageable
    ) {
        Page<DiscussionResponse> page = collaborationService.getDiscussions(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }
}
