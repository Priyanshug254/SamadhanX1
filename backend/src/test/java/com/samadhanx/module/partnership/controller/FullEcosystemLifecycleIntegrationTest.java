package com.samadhanx.module.partnership.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhanx.module.auth.dto.AuthResponse;
import com.samadhanx.module.auth.dto.LoginRequest;
import com.samadhanx.module.auth.dto.RegisterRequest;
import com.samadhanx.module.challenge.dto.ChallengeResponse;
import com.samadhanx.module.challenge.dto.EscalateToInnovationRequest;
import com.samadhanx.module.challenge.dto.SubmitChallengeRequest;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;
import com.samadhanx.module.organization.dto.FacultyProfileRequest;
import com.samadhanx.module.organization.dto.InstitutionalResourceRequest;
import com.samadhanx.module.organization.dto.OrganizationResponse;
import com.samadhanx.module.organization.dto.RegisterOrganizationRequest;
import com.samadhanx.module.organization.dto.ReviewVerificationRequest;
import com.samadhanx.module.organization.dto.SubmitVerificationRequest;
import com.samadhanx.module.organization.dto.SupportingDocumentRequest;
import com.samadhanx.module.organization.dto.UniversityProfileRequest;
import com.samadhanx.module.organization.entity.enums.DocumentType;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import com.samadhanx.module.organization.entity.enums.InstitutionType;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.ResourceType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.partnership.dto.*;
import com.samadhanx.module.partnership.entity.enums.*;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.role.repository.RoleRepository;
import com.samadhanx.module.solution.dto.*;
import com.samadhanx.module.solution.entity.enums.EvaluationRecommendation;
import com.samadhanx.module.solution.entity.enums.ProposalDocumentType;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.solution.entity.enums.TeamRole;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Milestone 5: Complete End-to-End Ecosystem Lifecycle Integration Test")
class FullEcosystemLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String studentLeadToken;
    private String facultyMentorToken;
    private String partnerToken;
    private String govOfficialToken;

    private UUID adminUserId;
    private UUID studentLeadUserId;
    private UUID facultyMentorUserId;
    private UUID partnerUserId;
    private UUID govOfficialUserId;

    private UUID verifiedUnivOrgId;
    private UUID verifiedPartnerOrgId;
    private UUID challengeId;
    private UUID proposalId;

    @BeforeEach
    void setUp() throws Exception {
        long ts = System.currentTimeMillis();

        // 1. Super Admin
        String adminEmail = "admin.m5." + ts + "@samadhanx.gov.in";
        Role superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleName.SUPER_ADMIN).description("Super Admin").build())
        );
        User adminUser = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("AdminPass@123"))
                .firstName("Super")
                .lastName("Admin")
                .isActive(true)
                .build();
        adminUser.addRole(superAdminRole);
        User savedAdmin = userRepository.save(adminUser);
        adminUserId = savedAdmin.getId();

        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder().email(adminEmail).password("AdminPass@123").build())))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readValue(objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("data").toString(), AuthResponse.class).getAccessToken();

        // 2. Student Lead
        RegisterRequest studentReq = RegisterRequest.builder()
                .email("rahul.m5." + ts + "@iitbhu.ac.in")
                .password("StudentPass@123")
                .firstName("Rahul")
                .lastName("Verma")
                .role(RoleName.STUDENT)
                .build();
        MvcResult sRes = mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(studentReq)))
                .andExpect(status().isCreated()).andReturn();
        AuthResponse sAuth = objectMapper.readValue(objectMapper.readTree(sRes.getResponse().getContentAsString()).get("data").toString(), AuthResponse.class);
        studentLeadToken = sAuth.getAccessToken();
        studentLeadUserId = sAuth.getUser().getId();

        // 3. Faculty Mentor
        RegisterRequest facultyReq = RegisterRequest.builder()
                .email("prof.iyer.m5." + ts + "@iitbhu.ac.in")
                .password("FacultyPass@123")
                .firstName("Dr. Anil")
                .lastName("Iyer")
                .role(RoleName.FACULTY)
                .build();
        MvcResult fRes = mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(facultyReq)))
                .andExpect(status().isCreated()).andReturn();
        AuthResponse fAuth = objectMapper.readValue(objectMapper.readTree(fRes.getResponse().getContentAsString()).get("data").toString(), AuthResponse.class);
        facultyMentorToken = fAuth.getAccessToken();
        facultyMentorUserId = fAuth.getUser().getId();

        // 4. Industry/Startup Partner
        RegisterRequest partnerReq = RegisterRequest.builder()
                .email("partner.vikram." + ts + "@ceramictech.co.in")
                .password("PartnerPass@123")
                .firstName("Vikram")
                .lastName("Malhotra")
                .role(RoleName.STARTUP)
                .build();
        MvcResult pRes = mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(partnerReq)))
                .andExpect(status().isCreated()).andReturn();
        AuthResponse pAuth = objectMapper.readValue(objectMapper.readTree(pRes.getResponse().getContentAsString()).get("data").toString(), AuthResponse.class);
        partnerToken = pAuth.getAccessToken();
        partnerUserId = pAuth.getUser().getId();

        // 5. Government Official
        String govEmail = "officer.sanjay." + ts + "@upjalnigam.gov.in";
        Role govRole = roleRepository.findByName(RoleName.GOVERNMENT_OFFICIAL).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleName.GOVERNMENT_OFFICIAL).description("Government Official").build())
        );
        User govUser = User.builder()
                .email(govEmail)
                .passwordHash(passwordEncoder.encode("GovPass@123"))
                .firstName("Sanjay")
                .lastName("Verma")
                .isActive(true)
                .build();
        govUser.addRole(govRole);
        User savedGov = userRepository.save(govUser);
        govOfficialUserId = savedGov.getId();

        MvcResult govRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder().email(govEmail).password("GovPass@123").build())))
                .andExpect(status().isOk())
                .andReturn();
        govOfficialToken = objectMapper.readValue(objectMapper.readTree(govRes.getResponse().getContentAsString()).get("data").toString(), AuthResponse.class).getAccessToken();

        // 6. Register & Verify University Organization
        RegisterOrganizationRequest orgReq = RegisterOrganizationRequest.builder()
                .name("Indian Institute of Technology (BHU) Varanasi")
                .code("IIT-BHU-M5-" + ts)
                .organizationType(OrganizationType.UNIVERSITY)
                .contactEmail("contact.m5." + ts + "@iitbhu.ac.in")
                .state("Uttar Pradesh")
                .district("Varanasi")
                .pincode("221005")
                .domainCodes(List.of("WATER_SANITATION", "CLEAN_ENERGY"))
                .primaryDomainCode("WATER_SANITATION")
                .build();
        MvcResult orgRes = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orgReq)))
                .andExpect(status().isCreated()).andReturn();
        verifiedUnivOrgId = objectMapper.readValue(objectMapper.readTree(orgRes.getResponse().getContentAsString()).get("data").toString(), OrganizationResponse.class).getId();

        // Add University Profile
        UniversityProfileRequest univProfileReq = UniversityProfileRequest.builder()
                .aisheCode("U-" + ts)
                .institutionType(InstitutionType.IIT_NIT_IIIT)
                .naacGrade("A++")
                .nirfRankRange("1-10")
                .hasIncubationCentre(true)
                .incubationCentreName("IIT BHU Discovery & Incubation Centre")
                .totalFacultyCount(520)
                .totalStudentCount(7100)
                .build();
        mockMvc.perform(put("/api/v1/organizations/" + verifiedUnivOrgId + "/university-profile")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(univProfileReq)))
                .andExpect(status().isOk());

        // Verify University
        SubmitVerificationRequest verifReq = SubmitVerificationRequest.builder()
                .organizationId(verifiedUnivOrgId)
                .documents(List.of(SupportingDocumentRequest.builder().documentType(DocumentType.AISHE_CERTIFICATE).documentName("aishe_cert_2026.pdf").documentUrl("https://docs.samadhanx.org/proofs/aishe_cert.pdf").build()))
                .build();
        MvcResult verifRes = mockMvc.perform(post("/api/v1/verifications").header("Authorization", "Bearer " + facultyMentorToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(verifReq)))
                .andExpect(status().isCreated()).andReturn();
        String verifId = objectMapper.readTree(verifRes.getResponse().getContentAsString()).get("data").get("id").asText();

        mockMvc.perform(post("/api/v1/verifications/" + verifId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReviewVerificationRequest.builder().decision(VerificationStatus.VERIFIED).reviewerNotes("AISHE verified").build())))
                .andExpect(status().isOk());

        // 7. Register & Verify Partner Organization (Startup / Industry)
        RegisterOrganizationRequest partnerOrgReq = RegisterOrganizationRequest.builder()
                .name("CeramicTech CleanWater Pvt Ltd")
                .code("CERAMIC-M5-" + ts)
                .organizationType(OrganizationType.STARTUP)
                .contactEmail("contact.m5." + ts + "@ceramictech.co.in")
                .state("Uttar Pradesh")
                .district("Varanasi")
                .pincode("221010")
                .domainCodes(List.of("WATER_SANITATION"))
                .primaryDomainCode("WATER_SANITATION")
                .build();
        MvcResult pOrgRes = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerOrgReq)))
                .andExpect(status().isCreated()).andReturn();
        verifiedPartnerOrgId = objectMapper.readValue(objectMapper.readTree(pOrgRes.getResponse().getContentAsString()).get("data").toString(), OrganizationResponse.class).getId();

        // Verify Partner Org
        SubmitVerificationRequest pVerifReq = SubmitVerificationRequest.builder()
                .organizationId(verifiedPartnerOrgId)
                .documents(List.of(SupportingDocumentRequest.builder().documentType(DocumentType.DPIIT_CERTIFICATE).documentName("dpiit_cert_2026.pdf").documentUrl("https://docs.samadhanx.org/proofs/dpiit.pdf").build()))
                .build();
        MvcResult pVerifRes = mockMvc.perform(post("/api/v1/verifications").header("Authorization", "Bearer " + partnerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(pVerifReq)))
                .andExpect(status().isCreated()).andReturn();
        String pVerifId = objectMapper.readTree(pVerifRes.getResponse().getContentAsString()).get("data").get("id").asText();

        mockMvc.perform(post("/api/v1/verifications/" + pVerifId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReviewVerificationRequest.builder().decision(VerificationStatus.VERIFIED).reviewerNotes("DPIIT verified").build())))
                .andExpect(status().isOk());

        // 8. Register Partner Capabilities
        PartnerCapabilityRequest capReq = PartnerCapabilityRequest.builder()
                .organizationId(verifiedPartnerOrgId)
                .sectors("Water & Sanitation, Cleantech, Nanomaterials")
                .technologies("Ceramic Membranes, Hydroxyapatite, Sintering Kilns")
                .areasOfInterest("Fluoride and Arsenic remediation, rural gravity filters")
                .mentoringCapability(true)
                .fundingCapability(true)
                .prototypingCapability(true)
                .testingCapability(true)
                .deploymentCapability(true)
                .geographicServiceAreas("Varanasi, Uttar Pradesh, National")
                .availableResourcesBudget(BigDecimal.valueOf(2500000))
                .build();
        mockMvc.perform(post("/api/v1/partners/capabilities")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(capReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fundingCapability", is(true)))
                .andExpect(jsonPath("$.data.prototypingCapability", is(true)));
    }

    @Test
    @DisplayName("Complete End-to-End SIH Lifecycle: Challenge -> Solution -> Partner Matching -> Funding -> Testing -> Pilot -> Impact -> Tech Transfer -> Government Oversight")
    void completeEndToEndEcosystemLifecycle() throws Exception {
        // Step 1: Submit Citizen Challenge & Escalate to INNOVATION_REQUIRED
        SubmitChallengeRequest chReq = SubmitChallengeRequest.builder()
                .title("Severe Fluoride and Arsenic Contamination in Rural Hand Pumps")
                .description("Groundwater testing shows 0.09 mg/L arsenic and 3.2 mg/L fluoride causing crippling skeletal fluorosis across 5 village panchayats.")
                .domainCode("WATER_SANITATION")
                .latitude(BigDecimal.valueOf(25.2820))
                .longitude(BigDecimal.valueOf(83.1150))
                .district("Varanasi")
                .state("Uttar Pradesh")
                .pincode("221005")
                .jurisdictionLevel(GovernmentLevel.PANCHAYAT_PRI)
                .severityLevel(SeverityLevel.CRITICAL)
                .urgencyLevel(UrgencyLevel.IMMEDIATE)
                .estimatedAffectedPopulation(8500)
                .build();

        MvcResult chRes = mockMvc.perform(post("/api/v1/challenges")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chReq)))
                .andExpect(status().isCreated())
                .andReturn();
        challengeId = objectMapper.readValue(objectMapper.readTree(chRes.getResponse().getContentAsString()).get("data").toString(), ChallengeResponse.class).getId();

        // Escalate Challenge to Innovation
        mockMvc.perform(post("/api/v1/challenges/" + challengeId + "/escalate-to-innovation")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EscalateToInnovationRequest.builder().escalationJustification("Standard reverse-osmosis solutions waste 70% water as reject brine; novel electricity-free adsorbent membrane required.").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("INNOVATION_REQUIRED")));

        // Step 2: Form Multidisciplinary Team & Submit Solution Proposal
        CreateTeamRequest teamReq = CreateTeamRequest.builder()
                .challengeId(challengeId)
                .homeUniversityId(verifiedUnivOrgId)
                .teamName("JalShuddhi Nanotech Innovation Team")
                .description("Interdisciplinary team from Chemistry, Materials Science and Civil Engineering")
                .build();
        MvcResult teamRes = mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID teamId = objectMapper.readValue(objectMapper.readTree(teamRes.getResponse().getContentAsString()).get("data").toString(), TeamResponse.class).getId();

        // Submit Solution Proposal
        SubmitProposalRequest propReq = SubmitProposalRequest.builder()
                .challengeId(challengeId)
                .teamId(teamId)
                .title("Gravity-Fed Terracotta Hydroxyapatite Nanocomposite Membrane Filter")
                .problemUnderstanding("High depth groundwater contains toxic arsenic and fluoride co-ions.")
                .proposedSolution("Low-cost porous ceramic candle embedded with synthetic hydroxyapatite nanoparticles.")
                .innovationNovelty("90% cheaper than commercial reverse osmosis, requires zero electricity.")
                .technicalApproach("Thermally sintered local clay matrix with 15% nano-hydroxyapatite adsorbent.")
                .expectedImpact("Provides 100% electricity-free, 4.8 L/hr potable water meeting IS 10500 standards for 3,500 rural villagers.")
                .implementationPlan("M1: Lab sintering; M2: QA water testing; M3: Panchayat pilot.")
                .requiredResources("Sintering kiln furnace, ICP-OES spectroscopy laboratory.")
                .estimatedCostInr(BigDecimal.valueOf(450000))
                .prototypeDescription("Terracotta candle unit with food-grade stainless steel gravity dispenser.")
                .documents(List.of(ProposalDocumentDto.builder().documentType(ProposalDocumentType.TECHNICAL_SPEC).documentName("Nanocomposite_CAD_Spec.pdf").documentUrl("https://docs.samadhanx.org/cad/spec.pdf").build()))
                .build();

        MvcResult propRes = mockMvc.perform(post("/api/v1/proposals")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propReq)))
                .andExpect(status().isCreated())
                .andReturn();
        proposalId = objectMapper.readValue(objectMapper.readTree(propRes.getResponse().getContentAsString()).get("data").toString(), ProposalResponse.class).getId();

        // Step 3: Expert Multi-Dimensional Scorecard Evaluation & State Transition to PROTOTYPING
        EvaluateProposalRequest evalReq = EvaluateProposalRequest.builder()
                .problemUnderstandingScore(90)
                .innovationScore(95)
                .technicalFeasibilityScore(85)
                .socialImpactScore(92)
                .scalabilityScore(88)
                .costEffectivenessScore(90)
                .sustainabilityScore(85)
                .implementationReadinessScore(85)
                .recommendation(EvaluationRecommendation.SHORTLIST)
                .qualitativeFeedback("Exceptional low-cost material science innovation. Highly recommended for incubation grant.")
                .build();

        mockMvc.perform(post("/api/v1/proposals/" + proposalId + "/evaluate")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.totalScore", greaterThan(80.0)));

        // Advance to SHORTLISTED -> PROTOTYPING
        mockMvc.perform(post("/api/v1/proposals/" + proposalId + "/state")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProposalStateUpdateRequest.builder().targetStatus(ProposalStatus.SHORTLISTED).notes("Shortlisted by jury").build())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/proposals/" + proposalId + "/state")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProposalStateUpdateRequest.builder().targetStatus(ProposalStatus.PROTOTYPING).notes("Prototyping initiated").build())))
                .andExpect(status().isOk());

        // Step 4: Smart Partner Matching (Explainable AI Engine)
        mockMvc.perform(get("/api/v1/partners/matching/proposal/" + proposalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].organizationName", is("CeramicTech CleanWater Pvt Ltd")))
                .andExpect(jsonPath("$.data[0].matchScore", greaterThanOrEqualTo(70.0)))
                .andExpect(jsonPath("$.data[0].matchingFactors", hasSize(greaterThanOrEqualTo(1))));

        // Step 5: Collaboration Request & Acceptance
        SubmitCollaborationRequest collabReq = SubmitCollaborationRequest.builder()
                .proposalId(proposalId)
                .partnerOrganizationId(verifiedPartnerOrgId)
                .collaborationType(CollaborationType.PROTOTYPING)
                .message("CeramicTech offers pilot sintering kiln access and molding support.")
                .proposedContribution("500 kiln hours and master technician oversight.")
                .nominatedContactPerson("Vikram Malhotra")
                .contactEmail("vikram@ceramictech.co.in")
                .build();

        MvcResult collabRes = mockMvc.perform(post("/api/v1/partners/collaborations/request")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(collabReq)))
                .andExpect(status().isCreated())
                .andReturn();
        String collabId = objectMapper.readTree(collabRes.getResponse().getContentAsString()).get("data").get("id").asText();

        // Accept Collaboration
        mockMvc.perform(post("/api/v1/partners/collaborations/requests/" + collabId + "/review")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReviewCollaborationRequest.builder().decision(CollaborationStatus.ACCEPTED).reviewRemarks("Accepted for pilot manufacturing").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("ACCEPTED")));

        // Step 6: Mentorship Engagement & Guidance Log
        InviteMentorRequest mentorReq = InviteMentorRequest.builder()
                .proposalId(proposalId)
                .mentorUserId(partnerUserId)
                .mentorOrganizationId(verifiedPartnerOrgId)
                .goalsAndObjectives("Guide ceramic sintering quality control & industrial scalability.")
                .invitationNotes("Welcome as industrial prototyping mentor.")
                .build();
        MvcResult mentorRes = mockMvc.perform(post("/api/v1/partners/mentorships/invite")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mentorReq)))
                .andExpect(status().isCreated())
                .andReturn();
        String mentorEngagementId = objectMapper.readTree(mentorRes.getResponse().getContentAsString()).get("data").get("id").asText();

        // Accept Mentorship
        mockMvc.perform(post("/api/v1/partners/mentorships/" + mentorEngagementId + "/accept")
                        .header("Authorization", "Bearer " + partnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mentorshipStatus", is("ACTIVE")));

        // Log Mentorship Session
        LogMentorshipActivityRequest logReq = LogMentorshipActivityRequest.builder()
                .sessionTitle("Design Review: Sintering Thermal Curve & Mold Assembly")
                .guidanceNotes("Advised ramp rate 5C/min to 920C to avoid micro-cracks.")
                .milestonesReviewed("Milestone 1: 50 prototype candle extrusions completed.")
                .actionItems("Test mechanical compression strength at IIT BHU civil engineering lab.")
                .build();
        mockMvc.perform(post("/api/v1/partners/mentorships/" + mentorEngagementId + "/logs")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sessionTitle", is(logReq.getSessionTitle())));

        // Step 7: CSR Funding Requirement & Offer Approval
        CreateFundingRequirementRequest fundReq = CreateFundingRequirementRequest.builder()
                .proposalId(proposalId)
                .requestedAmountInr(BigDecimal.valueOf(350000))
                .purpose("Procurement of Hydroxyapatite Nanoparticles & Pilot Stainless Steel Dispensers")
                .category(FundingCategory.PROTOTYPING_MATERIAL)
                .justification("Required for synthesizing 200 prototype filtration units.")
                .build();
        MvcResult fundReqRes = mockMvc.perform(post("/api/v1/partners/funding/requirements")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fundReq)))
                .andExpect(status().isCreated())
                .andReturn();
        String requirementId = objectMapper.readTree(fundReqRes.getResponse().getContentAsString()).get("data").get("id").asText();

        // Submit Funding Offer
        SubmitFundingOfferRequest offerReq = SubmitFundingOfferRequest.builder()
                .requirementId(UUID.fromString(requirementId))
                .sponsorOrganizationId(verifiedPartnerOrgId)
                .offeredAmountInr(BigDecimal.valueOf(350000))
                .supportType(FundingSupportType.MONETARY_GRANT)
                .termsAndConditions("Standard CSR startup innovation grant.")
                .build();
        MvcResult offerRes = mockMvc.perform(post("/api/v1/partners/funding/offers")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerReq)))
                .andExpect(status().isCreated())
                .andReturn();
        String offerId = objectMapper.readTree(offerRes.getResponse().getContentAsString()).get("data").get("id").asText();

        // Approve & Disburse Funding
        ReviewFundingOfferRequest fundReviewReq = ReviewFundingOfferRequest.builder()
                .decision(FundingOfferStatus.DISBURSED)
                .disbursedAmountInr(BigDecimal.valueOf(350000))
                .utilizationReport("Full tranche of ₹3,50,000 disbursed for raw materials and molds.")
                .build();
        mockMvc.perform(post("/api/v1/partners/funding/offers/" + offerId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fundReviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("DISBURSED")))
                .andExpect(jsonPath("$.data.disbursedAmountInr", notNullValue()));

        // Step 8: Laboratory Validation Test (NABL Water Testing - PASSED)
        SubmitValidationTestRequest valTestReq = SubmitValidationTestRequest.builder()
                .proposalId(proposalId)
                .testType(TestType.WATER_QUALITY_ANALYSIS)
                .testEnvironment("NABL Accredited Hydrogeology Testing Laboratory, IIT BHU")
                .evaluatorName("Dr. S. K. Roy (Lead Auditor)")
                .parametersTested("Arsenic reduced from 0.09 mg/L to 0.003 mg/L (IS 10500 compliant). Fluoride reduced from 3.2 mg/L to 0.6 mg/L. Flow rate: 4.8 L/hr.")
                .testResult(TestResult.PASSED)
                .validationRemarks("Passed all Bureau of Indian Standards IS 10500 drinking water parameters.")
                .evidenceDocumentUrl("https://docs.samadhanx.org/tests/nabl_jal_report_2026.pdf")
                .build();
        mockMvc.perform(post("/api/v1/pilots/validation-tests")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valTestReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.testResult", is("PASSED")));

        // Step 9: Advance Proposal State to PILOT_READY & Establish Pilot Project
        mockMvc.perform(post("/api/v1/proposals/" + proposalId + "/state")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProposalStateUpdateRequest.builder().targetStatus(ProposalStatus.PILOT_READY).notes("Laboratory validation passed; authorized for field pilot deployment").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PILOT_READY")));

        // Create Pilot Deployment
        CreatePilotProjectRequest pilotReq = CreatePilotProjectRequest.builder()
                .proposalId(proposalId)
                .locationName("Chiraigaon Gram Panchayat & Primary Health Sub-Center")
                .district("Varanasi")
                .state("Uttar Pradesh")
                .pincode("221112")
                .targetPopulation(3500)
                .implementationPartnerId(verifiedPartnerOrgId)
                .objectives("Deploy 15 gravity-fed ceramic filtration units across 5 Anganwadi centers.")
                .build();
        MvcResult pilotRes = mockMvc.perform(post("/api/v1/pilots")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pilotReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("PLANNED")))
                .andExpect(jsonPath("$.data.pilotCode", startsWith("PLT-")))
                .andReturn();
        String pilotId = objectMapper.readTree(pilotRes.getResponse().getContentAsString()).get("data").get("id").asText();

        // Advance Pilot to ACTIVE -> COMPLETED with Community Validation
        mockMvc.perform(post("/api/v1/pilots/" + pilotId + "/status")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdatePilotStatusRequest.builder().status(PilotStatus.ACTIVE).communityValidationStatus(CommunityValidationStatus.VALIDATED).feedbackNotes("15 units operational; 3,500 villagers accessing pure water daily.").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        mockMvc.perform(post("/api/v1/pilots/" + pilotId + "/status")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdatePilotStatusRequest.builder().status(PilotStatus.COMPLETED).communityValidationStatus(CommunityValidationStatus.VALIDATED).feedbackNotes("Pilot successfully concluded with zero recorded filter breakdowns.").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")));

        // Step 10: Record Measurable Social Impact KPIs & Government Verification
        RecordImpactMetricRequest metric1 = RecordImpactMetricRequest.builder()
                .proposalId(proposalId)
                .pilotId(UUID.fromString(pilotId))
                .kpiName(KpiName.PEOPLE_BENEFITED)
                .baselineValue(BigDecimal.ZERO)
                .targetValue(BigDecimal.valueOf(3500))
                .actualValue(BigDecimal.valueOf(3650))
                .unitOfMeasure("Persons")
                .evidenceUrl("https://docs.samadhanx.org/impact/chiraigaon_census_verification.pdf")
                .remarks("Verified by Gram Panchayat register.")
                .build();
        MvcResult m1Res = mockMvc.perform(post("/api/v1/pilots/impact-metrics")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metric1)))
                .andExpect(status().isCreated())
                .andReturn();
        String metricId = objectMapper.readTree(m1Res.getResponse().getContentAsString()).get("data").get("id").asText();

        RecordImpactMetricRequest metric2 = RecordImpactMetricRequest.builder()
                .proposalId(proposalId)
                .pilotId(UUID.fromString(pilotId))
                .kpiName(KpiName.WATER_SAVED_LITERS_PER_DAY)
                .baselineValue(BigDecimal.ZERO)
                .targetValue(BigDecimal.valueOf(15000))
                .actualValue(BigDecimal.valueOf(17500))
                .unitOfMeasure("Liters/Day")
                .remarks("Purified potable water delivered daily with zero reject wastewater.")
                .build();
        mockMvc.perform(post("/api/v1/pilots/impact-metrics")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metric2)))
                .andExpect(status().isCreated());

        // Government Official verifies Impact Metric
        mockMvc.perform(post("/api/v1/pilots/impact-metrics/" + metricId + "/verify")
                        .header("Authorization", "Bearer " + govOfficialToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(VerifyImpactMetricRequest.builder().verificationStatus(MetricVerificationStatus.VERIFIED_BY_GOVERNMENT).remarks("Field audited and verified by UP Jal Nigam district engineer.").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus", is("VERIFIED_BY_GOVERNMENT")));

        // Step 11: Technology Transfer & Commercialization Licensing Record
        RecordTechTransferRequest ttReq = RecordTechTransferRequest.builder()
                .proposalId(proposalId)
                .assetName("JalShuddhi Hydroxyapatite Nanocomposite Porous Candle IP & Manufacturing Protocol")
                .ipRegistrationNumber("IN-PAT-2026-99881")
                .licensingType(LicensingType.NON_EXCLUSIVE)
                .receivingOrganizationId(verifiedPartnerOrgId)
                .responsibleParties("IIT (BHU) Varanasi (Licensor), CeramicTech CleanWater Pvt Ltd (Licensee), UP Jal Nigam (Deployment Oversight)")
                .deploymentStatus(TechTransferDeploymentStatus.COMMERCIALIZED)
                .documentationUrl("https://docs.samadhanx.org/techtransfer/agreement_jal_2026.pdf")
                .build();
        mockMvc.perform(post("/api/v1/pilots/tech-transfer")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ttReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.assetName", is(ttReq.getAssetName())))
                .andExpect(jsonPath("$.data.licensingType", is("NON_EXCLUSIVE")));

        // Step 12: High-Level Government Oversight Dashboard Aggregation
        mockMvc.perform(get("/api/v1/government/oversight")
                        .header("Authorization", "Bearer " + govOfficialToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalChallenges", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.participatingUniversities", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.verifiedStartups", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalApprovedFundingInr", greaterThanOrEqualTo(350000.0)))
                .andExpect(jsonPath("$.data.totalPopulationBenefited", greaterThanOrEqualTo(3500)))
                .andExpect(jsonPath("$.data.techTransfersCount", greaterThanOrEqualTo(1)));
    }
}
