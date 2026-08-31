package com.samadhanx.module.partnership.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.partnership.dto.PartnerCapabilityRequest;
import com.samadhanx.module.partnership.dto.PartnerCapabilityResponse;
import com.samadhanx.module.partnership.dto.PartnerMatchResponse;
import com.samadhanx.module.partnership.entity.PartnerCapability;
import com.samadhanx.module.partnership.repository.PartnerCapabilityRepository;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.repository.ProposalRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Partner Matching Service Tests")
class PartnerMatchingServiceTest {

    @Mock
    private PartnerCapabilityRepository partnerCapabilityRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PartnerMatchingServiceImpl partnerMatchingService;

    private User adminUser;
    private Organization verifiedPartnerOrg;
    private Organization unverifiedOrg;
    private Proposal proposal;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(UUID.randomUUID()).email("admin@samadhanx.gov.in").firstName("Platform").lastName("Admin").build();

        verifiedPartnerOrg = Organization.builder()
                .id(UUID.randomUUID())
                .name("CeramicTech CleanWater Pvt Ltd")
                .code("CERAMIC-TECH")
                .organizationType(OrganizationType.STARTUP)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        unverifiedOrg = Organization.builder()
                .id(UUID.randomUUID())
                .name("Unverified Corp")
                .code("UNVERIFIED")
                .organizationType(OrganizationType.INDUSTRY)
                .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                .build();

        Domain waterDomain = Domain.builder().id(UUID.randomUUID()).code("WATER_SANITATION").name("Water & Sanitation").build();
        Challenge challenge = Challenge.builder().id(UUID.randomUUID()).domain(waterDomain).district("Varanasi").state("Uttar Pradesh").build();

        proposal = Proposal.builder()
                .id(UUID.randomUUID())
                .challenge(challenge)
                .title("Gravity-Fed Terracotta Hydroxyapatite Nanocomposite Membrane Filter")
                .technicalApproach("Low cost porous ceramic candle extrusion with hydroxyapatite nanoparticles for fluoride remediation.")
                .build();
    }

    @Test
    @DisplayName("Verified organization can register partner capabilities successfully")
    void registerPartnerCapability_Success() {
        PartnerCapabilityRequest req = PartnerCapabilityRequest.builder()
                .organizationId(verifiedPartnerOrg.getId())
                .sectors("Water Purification, Cleantech, Nanotechnology")
                .technologies("Ceramic Membranes, Hydroxyapatite, Sintering Kilns")
                .areasOfInterest("Rural potable water, Fluoride remediation")
                .mentoringCapability(true)
                .fundingCapability(true)
                .prototypingCapability(true)
                .testingCapability(true)
                .deploymentCapability(true)
                .geographicServiceAreas("Varanasi, Uttar Pradesh, National")
                .availableResourcesBudget(BigDecimal.valueOf(2500000))
                .build();

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(organizationRepository.findById(verifiedPartnerOrg.getId())).thenReturn(Optional.of(verifiedPartnerOrg));
        when(partnerCapabilityRepository.findById(verifiedPartnerOrg.getId())).thenReturn(Optional.empty());
        when(partnerCapabilityRepository.save(any(PartnerCapability.class))).thenAnswer(i -> i.getArgument(0));

        PartnerCapabilityResponse resp = partnerMatchingService.registerOrUpdatePartnerCapability(req, adminUser.getId());

        assertThat(resp).isNotNull();
        assertThat(resp.getOrganizationName()).isEqualTo("CeramicTech CleanWater Pvt Ltd");
        assertThat(resp.isFundingCapability()).isTrue();
        assertThat(resp.isPrototypingCapability()).isTrue();
    }

    @Test
    @DisplayName("Unverified organization is rejected from registering partner capabilities")
    void registerPartnerCapability_Unverified_ThrowsException() {
        PartnerCapabilityRequest req = PartnerCapabilityRequest.builder()
                .organizationId(unverifiedOrg.getId())
                .sectors("Cleantech")
                .build();

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(organizationRepository.findById(unverifiedOrg.getId())).thenReturn(Optional.of(unverifiedOrg));

        assertThatThrownBy(() -> partnerMatchingService.registerOrUpdatePartnerCapability(req, adminUser.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("VERIFIED");
    }

    @Test
    @DisplayName("Smart matching engine computes explainable high match score for relevant partner")
    void findMatchingPartnersForProposal_CalculatesExplainableScore() {
        PartnerCapability cap = PartnerCapability.builder()
                .organizationId(verifiedPartnerOrg.getId())
                .organization(verifiedPartnerOrg)
                .sectors("Water & Sanitation, Cleantech, Water Purification")
                .technologies("Ceramic Membranes, Nanotechnology, Sintering")
                .areasOfInterest("Fluoride remediation, rural drinking water")
                .mentoringCapability(true)
                .fundingCapability(true)
                .prototypingCapability(true)
                .testingCapability(true)
                .deploymentCapability(true)
                .geographicServiceAreas("Varanasi, Uttar Pradesh")
                .availableResourcesBudget(BigDecimal.valueOf(1000000))
                .build();

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(partnerCapabilityRepository.findVerifiedPartnerCapabilities()).thenReturn(List.of(cap));

        List<PartnerMatchResponse> matches = partnerMatchingService.findMatchingPartnersForProposal(proposal.getId());

        assertThat(matches).hasSize(1);
        PartnerMatchResponse topMatch = matches.get(0);
        assertThat(topMatch.getOrganizationName()).isEqualTo("CeramicTech CleanWater Pvt Ltd");
        assertThat(topMatch.getMatchScore()).isGreaterThanOrEqualTo(70.0);
        assertThat(topMatch.getMatchTier()).isIn("GOOD", "EXCELLENT");
        assertThat(topMatch.getMatchingFactors()).isNotEmpty();
    }
}
