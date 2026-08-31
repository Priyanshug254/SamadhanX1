package com.samadhanx.module.solution.service;

import com.samadhanx.module.solution.dto.EvaluateProposalRequest;
import com.samadhanx.module.solution.dto.ProposalEvaluationResponse;

import java.util.List;
import java.util.UUID;

public interface ProposalEvaluationService {

    ProposalEvaluationResponse evaluateProposal(UUID proposalId, EvaluateProposalRequest request, UUID evaluatorUserId);

    List<ProposalEvaluationResponse> getEvaluationsForProposal(UUID proposalId);
}
