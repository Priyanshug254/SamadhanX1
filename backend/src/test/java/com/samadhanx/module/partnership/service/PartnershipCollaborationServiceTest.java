package com.samadhanx.module.partnership.service;

import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.partnership.dto.*;
import com.samadhanx.module.partnership.entity.*;
import com.samadhanx.module.partnership.entity.enums.CollaborationStatus;
import com.samadhanx.module.partnership.entity.enums.CollaborationType;
import com.samadhanx.module.partnership.entity.enums.FundingCategory;
import com.samadhanx.module.partnership.entity.enums.FundingOfferStatus;
import com.samadhanx.module.partnership.entity.enums.FundingSupportType;
import com.samadhanx.module.partnership.entity.enums.MentorshipStatus;
import com.samadhanx.module.partnership.repository.*;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.ProposalTimelineEventRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Partnership & Collaboration Service Tests")
class PartnershipCollaborationServiceTest {

    @Mock
    private CollaborationOpportunityRepository opportunityRepository;
    @Mock
    private CollaborationRequestRepository requestRepository;
    @Mock
    private MentorshipEngagementRepository mentorshipRepository;
    @Mock
    private MentorshipLogRepository mentorshipLogRepository;
    @Mock
    private FundingRequirementRepository fundingRequirementRepository;
    @Mock
    private FundingOfferRepository fundingOfferRepository;
    @Mock
    private CoDevelopmentProjectRepository coDevProjectRepository;
    @Mock
    private CoDevMilestoneRepository coDevMilestoneRepository;
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProposalTimelineEventRepository timelineEventRepository;

    @InjectMocks
    private PartnershipCollaborationServiceImpl collaborationService;

    private User leadStudent;
    private User facultyMentor;
    private Organization partnerOrg;
    private Proposal proposal;

    @BeforeEach
    void setUp() {
        leadStudent = User.builder().id(UUID.randomUUID()).email("student@iitbhu.ac.in").firstName("Rahul").lastName("Verma").build();
        leadStudent.addRole(Role.builder().name(RoleName.STUDENT).build());

        facultyMentor = User.builder().id(UUID.randomUUID()).email("mentor@iitbhu.ac.in").firstName("Dr. Anil").lastName("Iyer").build();
        facultyMentor.addRole(Role.builder().name(RoleName.FACULTY).build());

        partnerOrg = Organization.builder()
                .id(UUID.randomUUID())
                .name("CeramicTech CleanWater Pvt Ltd")
                .code("CERAMIC-TECH")
                .organizationType(OrganizationType.STARTUP)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        proposal = Proposal.builder()
                .id(UUID.randomUUID())
                .trackingNumber("PRP-2026-08-99999")
                .title("Low-Cost Ceramic Water Filter")
                .build();
    }

    @Test
    @DisplayName("Submit and Review Collaboration Request Lifecycle")
    void collaborationRequestLifecycle() {
        SubmitCollaborationRequest submitReq = SubmitCollaborationRequest.builder()
                .proposalId(proposal.getId())
                .partnerOrganizationId(partnerOrg.getId())
                .collaborationType(CollaborationType.PROTOTYPING)
                .message("Offering pilot sintering kiln capacity")
                .build();

        when(userRepository.findById(leadStudent.getId())).thenReturn(Optional.of(leadStudent));
        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(organizationRepository.findById(partnerOrg.getId())).thenReturn(Optional.of(partnerOrg));
        when(requestRepository.save(any(CollaborationRequest.class))).thenAnswer(i -> i.getArgument(0));

        CollaborationRequestResponse res = collaborationService.submitCollaborationRequest(submitReq, leadStudent.getId());
        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo(CollaborationStatus.REQUESTED);

        // Review & Accept
        CollaborationRequest existingCr = CollaborationRequest.builder()
                .id(UUID.randomUUID())
                .proposal(proposal)
                .partnerOrganization(partnerOrg)
                .collaborationType(CollaborationType.PROTOTYPING)
                .status(CollaborationStatus.REQUESTED)
                .build();

        when(requestRepository.findById(existingCr.getId())).thenReturn(Optional.of(existingCr));
        ReviewCollaborationRequest revReq = ReviewCollaborationRequest.builder()
                .decision(CollaborationStatus.ACCEPTED)
                .reviewRemarks("Accepted for pilot manufacturing")
                .build();

        CollaborationRequestResponse revRes = collaborationService.reviewCollaborationRequest(existingCr.getId(), revReq, leadStudent.getId());
        assertThat(revRes.getStatus()).isEqualTo(CollaborationStatus.ACCEPTED);
        assertThat(revRes.getReviewRemarks()).isEqualTo("Accepted for pilot manufacturing");
    }

    @Test
    @DisplayName("Funding Requirement & Offer Approval Workflow")
    void fundingLifecycle() {
        CreateFundingRequirementRequest req = CreateFundingRequirementRequest.builder()
                .proposalId(proposal.getId())
                .requestedAmountInr(BigDecimal.valueOf(350000))
                .purpose("Nanomaterial synthesis & ceramic molds")
                .category(FundingCategory.PROTOTYPING_MATERIAL)
                .justification("Required for 200 prototype candles")
                .build();

        when(userRepository.findById(leadStudent.getId())).thenReturn(Optional.of(leadStudent));
        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(fundingRequirementRepository.save(any(FundingRequirement.class))).thenAnswer(i -> i.getArgument(0));

        FundingRequirementResponse frResp = collaborationService.createFundingRequirement(req, leadStudent.getId());
        assertThat(frResp).isNotNull();
        assertThat(frResp.getRequestedAmountInr()).isEqualByComparingTo(BigDecimal.valueOf(350000));

        // Submit Offer
        FundingRequirement savedReq = FundingRequirement.builder()
                .id(UUID.randomUUID())
                .proposal(proposal)
                .requestedAmountInr(BigDecimal.valueOf(350000))
                .build();

        SubmitFundingOfferRequest offerReq = SubmitFundingOfferRequest.builder()
                .requirementId(savedReq.getId())
                .sponsorOrganizationId(partnerOrg.getId())
                .offeredAmountInr(BigDecimal.valueOf(350000))
                .supportType(FundingSupportType.MONETARY_GRANT)
                .termsAndConditions("Standard CSR innovation grant")
                .build();

        when(userRepository.findById(facultyMentor.getId())).thenReturn(Optional.of(facultyMentor));
        when(fundingRequirementRepository.findById(savedReq.getId())).thenReturn(Optional.of(savedReq));
        when(organizationRepository.findById(partnerOrg.getId())).thenReturn(Optional.of(partnerOrg));
        when(fundingOfferRepository.save(any(FundingOffer.class))).thenAnswer(i -> i.getArgument(0));

        FundingOfferResponse offerResp = collaborationService.submitFundingOffer(offerReq, facultyMentor.getId());
        assertThat(offerResp.getOfferedAmountInr()).isEqualByComparingTo(BigDecimal.valueOf(350000));
        assertThat(offerResp.getStatus()).isEqualTo(FundingOfferStatus.REQUESTED);
    }
}
