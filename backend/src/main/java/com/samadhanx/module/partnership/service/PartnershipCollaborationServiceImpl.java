package com.samadhanx.module.partnership.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.partnership.dto.*;
import com.samadhanx.module.partnership.entity.*;
import com.samadhanx.module.partnership.entity.enums.CollaborationStatus;
import com.samadhanx.module.partnership.entity.enums.FundingOfferStatus;
import com.samadhanx.module.partnership.entity.enums.MentorshipStatus;
import com.samadhanx.module.partnership.repository.*;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.ProposalTimelineEvent;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.ProposalTimelineEventRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnershipCollaborationServiceImpl implements PartnershipCollaborationService {

    private static final Logger log = LoggerFactory.getLogger(PartnershipCollaborationServiceImpl.class);

    private final CollaborationOpportunityRepository opportunityRepository;
    private final CollaborationRequestRepository requestRepository;
    private final MentorshipEngagementRepository mentorshipRepository;
    private final MentorshipLogRepository mentorshipLogRepository;
    private final FundingRequirementRepository fundingRequirementRepository;
    private final FundingOfferRepository fundingOfferRepository;
    private final CoDevelopmentProjectRepository coDevProjectRepository;
    private final CoDevMilestoneRepository coDevMilestoneRepository;
    private final ProposalRepository proposalRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProposalTimelineEventRepository timelineEventRepository;

    // ── Opportunities ─────────────────────────────────────────────
    @Override
    @Transactional
    public OpportunityResponse createOpportunity(CreateOpportunityRequest request, UUID userId) {
        User user = getUser(userId);
        Proposal proposal = getProposal(request.getProposalId());

        CollaborationOpportunity opp = CollaborationOpportunity.builder()
                .proposal(proposal)
                .title(request.getTitle())
                .description(request.getDescription())
                .collaborationType(request.getCollaborationType())
                .skillsSought(request.getSkillsSought())
                .requiredResources(request.getRequiredResources())
                .targetSectors(request.getTargetSectors())
                .isOpen(true)
                .createdBy(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CollaborationOpportunity saved = opportunityRepository.save(opp);
        log.info("Created collaboration opportunity: '{}' for proposal: {}", saved.getTitle(), proposal.getTrackingNumber());
        return OpportunityResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpportunityResponse> getOpportunitiesForProposal(UUID proposalId) {
        return opportunityRepository.findByProposalId(proposalId).stream()
                .map(OpportunityResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpportunityResponse> getOpenOpportunities() {
        return opportunityRepository.findByIsOpenTrue().stream()
                .map(OpportunityResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Collaboration Requests ────────────────────────────────────
    @Override
    @Transactional
    public CollaborationRequestResponse submitCollaborationRequest(SubmitCollaborationRequest request, UUID userId) {
        User user = getUser(userId);
        Proposal proposal = getProposal(request.getProposalId());
        Organization partnerOrg = organizationRepository.findById(request.getPartnerOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner organization not found"));

        if (partnerOrg.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Partner organization must be officially VERIFIED to engage in collaboration requests");
        }

        CollaborationOpportunity opp = null;
        if (request.getOpportunityId() != null) {
            opp = opportunityRepository.findById(request.getOpportunityId()).orElse(null);
        }

        CollaborationRequest cr = CollaborationRequest.builder()
                .opportunity(opp)
                .proposal(proposal)
                .partnerOrganization(partnerOrg)
                .initiatedByPartner(true)
                .collaborationType(request.getCollaborationType())
                .status(CollaborationStatus.REQUESTED)
                .message(request.getMessage())
                .proposedContribution(request.getProposedContribution())
                .nominatedContactPerson(request.getNominatedContactPerson())
                .contactEmail(request.getContactEmail())
                .createdBy(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CollaborationRequest saved = requestRepository.save(cr);
        recordTimeline(proposal, "Collaboration Requested: " + partnerOrg.getName(),
                String.format("Organization %s submitted a %s collaboration proposal.", partnerOrg.getName(), request.getCollaborationType()), user);

        log.info("Submitted collaboration request for proposal {} from partner {}", proposal.getTrackingNumber(), partnerOrg.getName());
        return CollaborationRequestResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public CollaborationRequestResponse reviewCollaborationRequest(UUID requestId, ReviewCollaborationRequest request, UUID userId) {
        User user = getUser(userId);
        CollaborationRequest cr = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration request not found"));

        cr.setStatus(request.getDecision());
        cr.setReviewRemarks(request.getReviewRemarks());
        cr.setUpdatedAt(Instant.now());

        CollaborationRequest saved = requestRepository.save(cr);
        recordTimeline(cr.getProposal(), "Collaboration " + request.getDecision().name() + ": " + cr.getPartnerOrganization().getName(),
                "Decision remarks: " + request.getReviewRemarks(), user);

        return CollaborationRequestResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollaborationRequestResponse> getRequestsForProposal(UUID proposalId) {
        return requestRepository.findByProposalId(proposalId).stream()
                .map(CollaborationRequestResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollaborationRequestResponse> getRequestsForPartner(UUID partnerOrgId) {
        return requestRepository.findByPartnerOrganizationId(partnerOrgId).stream()
                .map(CollaborationRequestResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Mentorship ────────────────────────────────────────────────
    @Override
    @Transactional
    public MentorshipEngagementResponse inviteMentor(InviteMentorRequest request, UUID userId) {
        User inviter = getUser(userId);
        Proposal proposal = getProposal(request.getProposalId());
        User mentor = getUser(request.getMentorUserId());

        Organization mentorOrg = null;
        if (request.getMentorOrganizationId() != null) {
            mentorOrg = organizationRepository.findById(request.getMentorOrganizationId()).orElse(null);
        }

        MentorshipEngagement me = MentorshipEngagement.builder()
                .proposal(proposal)
                .mentorUser(mentor)
                .mentorOrganization(mentorOrg)
                .mentorshipStatus(MentorshipStatus.INVITED)
                .goalsAndObjectives(request.getGoalsAndObjectives())
                .invitationNotes(request.getInvitationNotes())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        MentorshipEngagement saved = mentorshipRepository.save(me);
        recordTimeline(proposal, "Mentor Invited: " + mentor.getFullName(),
                "Invited as project mentor. Objectives: " + request.getGoalsAndObjectives(), inviter);

        return MentorshipEngagementResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public MentorshipEngagementResponse acceptMentorship(UUID engagementId, UUID userId) {
        User mentor = getUser(userId);
        MentorshipEngagement me = mentorshipRepository.findById(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorship engagement not found"));

        if (!me.getMentorUser().getId().equals(userId)) {
            throw new ForbiddenException("Only the invited mentor can accept this engagement");
        }

        me.setMentorshipStatus(MentorshipStatus.ACTIVE);
        me.setUpdatedAt(Instant.now());
        MentorshipEngagement saved = mentorshipRepository.save(me);

        recordTimeline(me.getProposal(), "Mentor Joined: " + mentor.getFullName(),
                mentor.getFullName() + " accepted the mentorship engagement.", mentor);

        return MentorshipEngagementResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public MentorshipEngagementResponse declineMentorship(UUID engagementId, UUID userId) {
        User mentor = getUser(userId);
        MentorshipEngagement me = mentorshipRepository.findById(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorship engagement not found"));

        if (!me.getMentorUser().getId().equals(userId)) {
            throw new ForbiddenException("Only the invited mentor can decline this engagement");
        }

        me.setMentorshipStatus(MentorshipStatus.DECLINED);
        me.setUpdatedAt(Instant.now());
        return MentorshipEngagementResponse.fromEntity(mentorshipRepository.save(me));
    }

    @Override
    @Transactional
    public MentorshipLogResponse logMentorshipActivity(UUID engagementId, LogMentorshipActivityRequest request, UUID userId) {
        User mentor = getUser(userId);
        MentorshipEngagement me = mentorshipRepository.findById(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorship engagement not found"));

        MentorshipLog logEntry = MentorshipLog.builder()
                .engagement(me)
                .mentorUser(mentor)
                .sessionTitle(request.getSessionTitle())
                .guidanceNotes(request.getGuidanceNotes())
                .milestonesReviewed(request.getMilestonesReviewed())
                .actionItems(request.getActionItems())
                .meetingDate(Instant.now())
                .createdAt(Instant.now())
                .build();

        MentorshipLog saved = mentorshipLogRepository.save(logEntry);
        recordTimeline(me.getProposal(), "Mentorship Review: " + request.getSessionTitle(),
                "Guidance recorded by " + mentor.getFullName() + ". Action items: " + request.getActionItems(), mentor);

        return MentorshipLogResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorshipEngagementResponse> getMentorshipsForProposal(UUID proposalId) {
        return mentorshipRepository.findByProposalId(proposalId).stream()
                .map(MentorshipEngagementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorshipLogResponse> getLogsForEngagement(UUID engagementId) {
        return mentorshipLogRepository.findByEngagementIdOrderByMeetingDateDesc(engagementId).stream()
                .map(MentorshipLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Funding ───────────────────────────────────────────────────
    @Override
    @Transactional
    public FundingRequirementResponse createFundingRequirement(CreateFundingRequirementRequest request, UUID userId) {
        User user = getUser(userId);
        Proposal proposal = getProposal(request.getProposalId());

        FundingRequirement fr = FundingRequirement.builder()
                .proposal(proposal)
                .requestedAmountInr(request.getRequestedAmountInr())
                .purpose(request.getPurpose())
                .category(request.getCategory())
                .justification(request.getJustification())
                .expectedDeliverables(request.getExpectedDeliverables())
                .proposedTimeline(request.getProposedTimeline())
                .isFulfilled(false)
                .createdBy(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        FundingRequirement saved = fundingRequirementRepository.save(fr);
        recordTimeline(proposal, "Funding Required: ₹" + request.getRequestedAmountInr(),
                "Created funding requirement for " + request.getPurpose(), user);

        return FundingRequirementResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public FundingOfferResponse submitFundingOffer(SubmitFundingOfferRequest request, UUID userId) {
        User user = getUser(userId);
        FundingRequirement req = fundingRequirementRepository.findById(request.getRequirementId())
                .orElseThrow(() -> new ResourceNotFoundException("Funding requirement not found"));

        Organization sponsor = organizationRepository.findById(request.getSponsorOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Sponsor organization not found"));

        if (sponsor.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Sponsor organization must be officially VERIFIED to offer funding");
        }

        FundingOffer offer = FundingOffer.builder()
                .requirement(req)
                .proposal(req.getProposal())
                .sponsorOrganization(sponsor)
                .offeredAmountInr(request.getOfferedAmountInr())
                .supportType(request.getSupportType())
                .status(FundingOfferStatus.REQUESTED)
                .termsAndConditions(request.getTermsAndConditions())
                .createdBy(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        FundingOffer saved = fundingOfferRepository.save(offer);
        recordTimeline(req.getProposal(), "Funding Offer Received: ₹" + request.getOfferedAmountInr(),
                String.format("Sponsor %s submitted a %s support offer.", sponsor.getName(), request.getSupportType()), user);

        return FundingOfferResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public FundingOfferResponse reviewFundingOffer(UUID offerId, ReviewFundingOfferRequest request, UUID userId) {
        User user = getUser(userId);
        FundingOffer offer = fundingOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Funding offer not found"));

        offer.setStatus(request.getDecision());
        if (request.getDisbursedAmountInr() != null && request.getDisbursedAmountInr().compareTo(BigDecimal.ZERO) > 0) {
            offer.setDisbursedAmountInr(request.getDisbursedAmountInr());
            offer.setDisbursedAt(Instant.now());
        }
        if (request.getUtilizationReport() != null) {
            offer.setUtilizationReport(request.getUtilizationReport());
        }
        if (request.getEvidenceDocumentUrl() != null) {
            offer.setEvidenceDocumentUrl(request.getEvidenceDocumentUrl());
        }
        offer.setUpdatedAt(Instant.now());

        if (request.getDecision() == FundingOfferStatus.APPROVED || request.getDecision() == FundingOfferStatus.DISBURSED) {
            offer.getRequirement().setFulfilled(true);
            fundingRequirementRepository.save(offer.getRequirement());
        }

        FundingOffer saved = fundingOfferRepository.save(offer);
        recordTimeline(offer.getProposal(), "Funding Offer " + request.getDecision().name(),
                String.format("Offer of ₹%s by %s is now %s.", offer.getOfferedAmountInr(), offer.getSponsorOrganization().getName(), request.getDecision()), user);

        return FundingOfferResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundingRequirementResponse> getFundingRequirementsForProposal(UUID proposalId) {
        return fundingRequirementRepository.findByProposalId(proposalId).stream()
                .map(FundingRequirementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundingOfferResponse> getFundingOffersForRequirement(UUID requirementId) {
        return fundingOfferRepository.findByRequirementId(requirementId).stream()
                .map(FundingOfferResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Co-Development ────────────────────────────────────────────
    @Override
    @Transactional
    public CoDevProjectResponse createCoDevProject(CreateCoDevProjectRequest request, UUID userId) {
        User user = getUser(userId);
        Proposal proposal = getProposal(request.getProposalId());
        Organization partner = organizationRepository.findById(request.getPartnerOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner organization not found"));

        CoDevelopmentProject cdp = CoDevelopmentProject.builder()
                .proposal(proposal)
                .partnerOrganization(partner)
                .title(request.getTitle())
                .objectives(request.getObjectives())
                .leadAcademicCoordinator(request.getLeadAcademicCoordinator())
                .leadIndustryCoordinator(request.getLeadIndustryCoordinator())
                .startDate(Instant.now())
                .targetCompletionDate(request.getTargetCompletionDate())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CoDevelopmentProject saved = coDevProjectRepository.save(cdp);
        recordTimeline(proposal, "Co-Development Initiated: " + partner.getName(),
                "Joint development project started: " + request.getTitle(), user);

        return CoDevProjectResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public CoDevMilestoneResponse addCoDevMilestone(UUID projectId, CreateCoDevMilestoneRequest request, UUID userId) {
        CoDevelopmentProject project = coDevProjectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Co-Development project not found"));

        CoDevMilestone m = CoDevMilestone.builder()
                .project(project)
                .milestoneName(request.getMilestoneName())
                .leadParty(request.getLeadParty())
                .deliverables(request.getDeliverables())
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .documentationUrl(request.getDocumentationUrl())
                .createdAt(Instant.now())
                .build();

        CoDevMilestone saved = coDevMilestoneRepository.save(m);
        return CoDevMilestoneResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoDevProjectResponse> getCoDevProjectsForProposal(UUID proposalId) {
        return coDevProjectRepository.findByProposalId(proposalId).stream()
                .map(CoDevProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Helper Methods ────────────────────────────────────────────
    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private Proposal getProposal(UUID proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + proposalId));
    }

    private void recordTimeline(Proposal proposal, String title, String message, User actor) {
        ProposalTimelineEvent event = ProposalTimelineEvent.builder()
                .proposal(proposal)
                .previousStatus(proposal.getStatus())
                .newStatus(proposal.getStatus())
                .actor(actor)
                .actorRole(actor.hasRole(RoleName.SUPER_ADMIN) ? "SUPER_ADMIN" :
                           actor.hasRole(RoleName.FACULTY) ? "FACULTY" :
                           actor.hasRole(RoleName.STUDENT) ? "STUDENT" : "PARTNER")
                .eventTitle(title)
                .eventMessage(message)
                .createdAt(Instant.now())
                .build();
        timelineEventRepository.save(event);
    }
}
