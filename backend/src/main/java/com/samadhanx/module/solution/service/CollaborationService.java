package com.samadhanx.module.solution.service;

import com.samadhanx.module.solution.dto.DashboardSummaryResponse;
import com.samadhanx.module.solution.dto.DiscussionResponse;
import com.samadhanx.module.solution.dto.PostDiscussionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CollaborationService {

    DiscussionResponse postDiscussion(UUID teamId, PostDiscussionRequest request, UUID senderUserId);

    Page<DiscussionResponse> getDiscussions(UUID teamId, Pageable pageable);

    DashboardSummaryResponse getDashboardSummary(UUID userId);
}
