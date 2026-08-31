package com.samadhanx.module.challenge.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ChallengeService {

    ChallengeResponse submitChallenge(SubmitChallengeRequest request, UUID submitterUserId);

    ChallengeResponse getChallengeById(UUID id);

    ChallengeResponse getChallengeByTrackingNumber(String trackingNumber);

    Page<ChallengeSummaryResponse> searchChallenges(
            UUID domainId,
            ChallengeStatus status,
            ResolutionPath resolutionPath,
            String state,
            String district,
            String searchQuery,
            Pageable pageable
    );

    Page<ChallengeSummaryResponse> getMySubmissions(UUID userId, Pageable pageable);

    Page<ChallengeSummaryResponse> getDepartmentAssignedQueue(UUID departmentId, Pageable pageable);

    EndorsementResponse endorseChallenge(UUID challengeId, EndorsementRequest request, UUID userId);

    ChallengeResponse performDepartmentAction(UUID challengeId, DepartmentActionRequest request, UUID officerUserId);

    ChallengeResponse resolveDepartmentalStandard(UUID challengeId, DepartmentResolveRequest request, UUID officerUserId);

    ChallengeResponse escalateToInnovation(UUID challengeId, EscalateToInnovationRequest request, UUID officerUserId);

    Page<ChallengeSummaryResponse> getInnovationPipeline(UUID domainId, Pageable pageable);

    List<UniversityChallengeMatchResponse> getMatchingChallengesForUniversity(UUID universityOrgId);

    List<TimelineEventResponse> getTimeline(UUID challengeId);
}
