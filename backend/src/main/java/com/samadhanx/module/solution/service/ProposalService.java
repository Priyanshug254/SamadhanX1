package com.samadhanx.module.solution.service;

import com.samadhanx.module.solution.dto.ProposalResponse;
import com.samadhanx.module.solution.dto.ProposalStateUpdateRequest;
import com.samadhanx.module.solution.dto.ProposalSummaryResponse;
import com.samadhanx.module.solution.dto.ProposalTimelineEventResponse;
import com.samadhanx.module.solution.dto.SubmitProposalRequest;
import com.samadhanx.module.solution.entity.ProposalTimelineEvent;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProposalService {

    ProposalResponse submitProposal(SubmitProposalRequest request, UUID submitterUserId);

    ProposalResponse getProposalById(UUID proposalId);

    ProposalResponse getProposalByTrackingNumber(String trackingNumber);

    List<ProposalResponse> getProposalsForChallenge(UUID challengeId);

    List<ProposalResponse> getRankedProposalsForChallenge(UUID challengeId);

    Page<ProposalSummaryResponse> searchProposals(ProposalStatus status, UUID challengeId, UUID hackathonId, Pageable pageable);

    ProposalResponse updateProposalState(UUID proposalId, ProposalStateUpdateRequest request, UUID actionByUserId);

    List<ProposalTimelineEventResponse> getProposalTimeline(UUID proposalId);
}
