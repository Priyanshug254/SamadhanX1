package com.samadhanx.module.partnership.service;

import com.samadhanx.module.partnership.dto.*;

import java.util.List;
import java.util.UUID;

public interface PartnershipCollaborationService {

    // Opportunities
    OpportunityResponse createOpportunity(CreateOpportunityRequest request, UUID userId);
    List<OpportunityResponse> getOpportunitiesForProposal(UUID proposalId);
    List<OpportunityResponse> getOpenOpportunities();

    // Collaboration Requests
    CollaborationRequestResponse submitCollaborationRequest(SubmitCollaborationRequest request, UUID userId);
    CollaborationRequestResponse reviewCollaborationRequest(UUID requestId, ReviewCollaborationRequest request, UUID userId);
    List<CollaborationRequestResponse> getRequestsForProposal(UUID proposalId);
    List<CollaborationRequestResponse> getRequestsForPartner(UUID partnerOrgId);

    // Mentorship
    MentorshipEngagementResponse inviteMentor(InviteMentorRequest request, UUID userId);
    MentorshipEngagementResponse acceptMentorship(UUID engagementId, UUID userId);
    MentorshipEngagementResponse declineMentorship(UUID engagementId, UUID userId);
    MentorshipLogResponse logMentorshipActivity(UUID engagementId, LogMentorshipActivityRequest request, UUID userId);
    List<MentorshipEngagementResponse> getMentorshipsForProposal(UUID proposalId);
    List<MentorshipLogResponse> getLogsForEngagement(UUID engagementId);

    // Funding
    FundingRequirementResponse createFundingRequirement(CreateFundingRequirementRequest request, UUID userId);
    FundingOfferResponse submitFundingOffer(SubmitFundingOfferRequest request, UUID userId);
    FundingOfferResponse reviewFundingOffer(UUID offerId, ReviewFundingOfferRequest request, UUID userId);
    List<FundingRequirementResponse> getFundingRequirementsForProposal(UUID proposalId);
    List<FundingOfferResponse> getFundingOffersForRequirement(UUID requirementId);

    // Co-Development
    CoDevProjectResponse createCoDevProject(CreateCoDevProjectRequest request, UUID userId);
    CoDevMilestoneResponse addCoDevMilestone(UUID projectId, CreateCoDevMilestoneRequest request, UUID userId);
    List<CoDevProjectResponse> getCoDevProjectsForProposal(UUID proposalId);
}
