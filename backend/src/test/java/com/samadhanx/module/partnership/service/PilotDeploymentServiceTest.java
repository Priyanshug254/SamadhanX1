package com.samadhanx.module.partnership.service;

import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.partnership.dto.*;
import com.samadhanx.module.partnership.entity.*;
import com.samadhanx.module.partnership.entity.enums.*;
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
@DisplayName("Pilot, Testing, Impact & Oversight Service Tests")
class PilotDeploymentServiceTest {

    @Mock
    private ValidationTestRepository validationTestRepository;
    @Mock
    private PilotProjectRepository pilotProjectRepository;
    @Mock
    private ImpactMetricRepository impactMetricRepository;
    @Mock
    private TechTransferRecordRepository techTransferRepository;
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private ProposalTimelineEventRepository timelineEventRepository;
    @Mock
    private FundingOfferRepository fundingOfferRepository;
    @Mock
    private CoDevelopmentProjectRepository coDevProjectRepository;
    @Mock
    private CollaborationRequestRepository collaborationRequestRepository;
    @Mock
    private MentorshipEngagementRepository mentorshipRepository;

    @InjectMocks
    private PilotDeploymentServiceImpl pilotDeploymentService;

    private User adminUser;
    private User govOfficialUser;
    private Proposal proposal;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(UUID.randomUUID()).email("admin@samadhanx.gov.in").firstName("Super").lastName("Admin").build();
        adminUser.addRole(Role.builder().name(RoleName.SUPER_ADMIN).build());

        govOfficialUser = User.builder().id(UUID.randomUUID()).email("official@upjalnigam.gov.in").firstName("Sanjay").lastName("Verma").build();
        govOfficialUser.addRole(Role.builder().name(RoleName.GOVERNMENT_OFFICIAL).build());

        proposal = Proposal.builder()
                .id(UUID.randomUUID())
                .trackingNumber("PRP-2026-08-11111")
                .title("Gravity-Fed Hydroxyapatite Nanocomposite Filter")
                .build();
    }

    @Test
    @DisplayName("Submit Laboratory Validation Test and Confirm Pass Status")
    void validationTestingWorkflow() {
        SubmitValidationTestRequest req = SubmitValidationTestRequest.builder()
                .proposalId(proposal.getId())
                .testType(TestType.WATER_QUALITY_ANALYSIS)
                .testEnvironment("NABL Water Lab IIT BHU")
                .evaluatorName("Dr. S. K. Roy")
                .parametersTested("Fluoride: 3.2 mg/L -> 0.6 mg/L, Arsenic: 0.09 mg/L -> 0.003 mg/L")
                .testResult(TestResult.PASSED)
                .validationRemarks("IS 10500 Compliant")
                .build();

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(validationTestRepository.save(any(ValidationTest.class))).thenAnswer(i -> i.getArgument(0));

        ValidationTestResponse resp = pilotDeploymentService.submitValidationTest(req, adminUser.getId());
        assertThat(resp).isNotNull();
        assertThat(resp.getTestResult()).isEqualTo(TestResult.PASSED);
        assertThat(resp.getTestType()).isEqualTo(TestType.WATER_QUALITY_ANALYSIS);

        when(validationTestRepository.existsByProposalIdAndTestResult(proposal.getId(), TestResult.PASSED)).thenReturn(true);
        assertThat(pilotDeploymentService.hasPassedValidation(proposal.getId())).isTrue();
    }

    @Test
    @DisplayName("Pilot Project Creation and Status Update Workflow")
    void pilotProjectWorkflow() {
        CreatePilotProjectRequest req = CreatePilotProjectRequest.builder()
                .proposalId(proposal.getId())
                .locationName("Chiraigaon Gram Panchayat")
                .district("Varanasi")
                .state("Uttar Pradesh")
                .pincode("221112")
                .targetPopulation(3500)
                .objectives("Deploy 15 ceramic filtration units across anganwadis")
                .build();

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(pilotProjectRepository.save(any(PilotProject.class))).thenAnswer(i -> i.getArgument(0));

        PilotProjectResponse pResp = pilotDeploymentService.createPilotProject(req, adminUser.getId());
        assertThat(pResp).isNotNull();
        assertThat(pResp.getLocationName()).isEqualTo("Chiraigaon Gram Panchayat");
        assertThat(pResp.getStatus()).isEqualTo(PilotStatus.PLANNED);
        assertThat(pResp.getPilotCode()).startsWith("PLT-");

        // Update to ACTIVE
        PilotProject existingPilot = PilotProject.builder()
                .id(UUID.randomUUID())
                .pilotCode("PLT-2026-08-12345")
                .proposal(proposal)
                .status(PilotStatus.PLANNED)
                .build();

        when(pilotProjectRepository.findById(existingPilot.getId())).thenReturn(Optional.of(existingPilot));
        UpdatePilotStatusRequest updateReq = UpdatePilotStatusRequest.builder()
                .status(PilotStatus.ACTIVE)
                .communityValidationStatus(CommunityValidationStatus.VALIDATED)
                .feedbackNotes("Community enthusiastic and actively using the filtration units.")
                .build();

        PilotProjectResponse updatedResp = pilotDeploymentService.updatePilotStatus(existingPilot.getId(), updateReq, adminUser.getId());
        assertThat(updatedResp.getStatus()).isEqualTo(PilotStatus.ACTIVE);
        assertThat(updatedResp.getCommunityValidationStatus()).isEqualTo(CommunityValidationStatus.VALIDATED);
    }

    @Test
    @DisplayName("Record Social Impact KPI and Government Verification")
    void impactMetricAndVerificationWorkflow() {
        RecordImpactMetricRequest metricReq = RecordImpactMetricRequest.builder()
                .proposalId(proposal.getId())
                .kpiName(KpiName.PEOPLE_BENEFITED)
                .baselineValue(BigDecimal.ZERO)
                .targetValue(BigDecimal.valueOf(3500))
                .actualValue(BigDecimal.valueOf(3650))
                .unitOfMeasure("Persons")
                .remarks("Field measured")
                .build();

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(impactMetricRepository.save(any(ImpactMetric.class))).thenAnswer(i -> i.getArgument(0));

        ImpactMetricResponse mResp = pilotDeploymentService.recordImpactMetric(metricReq, adminUser.getId());
        assertThat(mResp).isNotNull();
        assertThat(mResp.getActualValue()).isEqualByComparingTo(BigDecimal.valueOf(3650));
        assertThat(mResp.getVerificationStatus()).isEqualTo(MetricVerificationStatus.REPORTED);

        // Government Verification
        ImpactMetric existingMetric = ImpactMetric.builder()
                .id(UUID.randomUUID())
                .proposal(proposal)
                .kpiName(KpiName.PEOPLE_BENEFITED)
                .verificationStatus(MetricVerificationStatus.REPORTED)
                .build();

        when(userRepository.findById(govOfficialUser.getId())).thenReturn(Optional.of(govOfficialUser));
        when(impactMetricRepository.findById(existingMetric.getId())).thenReturn(Optional.of(existingMetric));

        VerifyImpactMetricRequest vReq = VerifyImpactMetricRequest.builder()
                .verificationStatus(MetricVerificationStatus.VERIFIED_BY_GOVERNMENT)
                .remarks("Audited on site by UP Jal Nigam")
                .build();

        ImpactMetricResponse vResp = pilotDeploymentService.verifyImpactMetric(existingMetric.getId(), vReq, govOfficialUser.getId());
        assertThat(vResp.getVerificationStatus()).isEqualTo(MetricVerificationStatus.VERIFIED_BY_GOVERNMENT);
        assertThat(vResp.getVerifiedByName()).isEqualTo("Sanjay Verma");
    }
}
