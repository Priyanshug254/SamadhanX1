package com.samadhanx.module.solution.controller;

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
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.dto.CreateTeamRequest;
import com.samadhanx.module.solution.dto.DashboardSummaryResponse;
import com.samadhanx.module.solution.dto.EvaluateProposalRequest;
import com.samadhanx.module.solution.dto.PostDiscussionRequest;
import com.samadhanx.module.solution.dto.ProposalDocumentDto;
import com.samadhanx.module.solution.dto.ProposalResponse;
import com.samadhanx.module.solution.dto.ProposalStateUpdateRequest;
import com.samadhanx.module.solution.dto.RespondInvitationRequest;
import com.samadhanx.module.solution.dto.SubmitProposalRequest;
import com.samadhanx.module.solution.dto.TeamResponse;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Milestone 4: Solution Development & Multidisciplinary Teams Integration Test")
class SolutionDevelopmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.samadhanx.module.role.repository.RoleRepository roleRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String adminToken;
    private String studentLeadToken;
    private String facultyMentorToken;
    private String evaluatorToken;

    private UUID adminUserId;
    private UUID studentLeadUserId;
    private UUID facultyMentorUserId;
    private UUID evaluatorUserId;

    private UUID verifiedUnivOrgId;
    private UUID challengeId;

    @BeforeEach
    void setUp() throws Exception {
        long ts = System.currentTimeMillis();

        // 1. Admin Setup
        String adminEmail = "admin.m4." + ts + "@samadhanx.gov.in";
        com.samadhanx.module.role.entity.Role superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN).orElseGet(() ->
                roleRepository.save(com.samadhanx.module.role.entity.Role.builder().name(RoleName.SUPER_ADMIN).description("Super Admin").build())
        );

        com.samadhanx.module.user.entity.User adminUser = com.samadhanx.module.user.entity.User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("AdminPass@123"))
                .firstName("Super")
                .lastName("Admin")
                .isActive(true)
                .build();
        adminUser.addRole(superAdminRole);
        com.samadhanx.module.user.entity.User savedAdmin = userRepository.save(adminUser);
        adminUserId = savedAdmin.getId();

        LoginRequest adminLogin = LoginRequest.builder()
                .email(adminEmail)
                .password("AdminPass@123")
                .build();

        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse adminAuth = objectMapper.readValue(
                objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class
        );
        adminToken = adminAuth.getAccessToken();

        // 2. Register Student Lead
        RegisterRequest studentReq = RegisterRequest.builder()
                .email("rahul.lead." + ts + "@iitbhu.ac.in")
                .password("StudentPass@123")
                .firstName("Rahul")
                .lastName("Verma")
                .role(RoleName.STUDENT)
                .build();
        MvcResult sRes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentReq)))
                .andExpect(status().isCreated())
                .andReturn();
        AuthResponse sAuth = objectMapper.readValue(
                objectMapper.readTree(sRes.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class
        );
        studentLeadToken = sAuth.getAccessToken();
        studentLeadUserId = sAuth.getUser().getId();

        // 3. Register Faculty Mentor
        RegisterRequest facultyReq = RegisterRequest.builder()
                .email("prof.iyer." + ts + "@iitbhu.ac.in")
                .password("FacultyPass@123")
                .firstName("Dr. Anil")
                .lastName("Iyer")
                .role(RoleName.FACULTY)
                .build();
        MvcResult fRes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyReq)))
                .andExpect(status().isCreated())
                .andReturn();
        AuthResponse fAuth = objectMapper.readValue(
                objectMapper.readTree(fRes.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class
        );
        facultyMentorToken = fAuth.getAccessToken();
        facultyMentorUserId = fAuth.getUser().getId();

        // 4. Register External Evaluator
        RegisterRequest evalReq = RegisterRequest.builder()
                .email("evaluator.dr.roy." + ts + "@dst.gov.in")
                .password("EvaluatorPass@123")
                .firstName("Dr. Sunita")
                .lastName("Roy")
                .role(RoleName.FACULTY)
                .build();
        MvcResult eRes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalReq)))
                .andExpect(status().isCreated())
                .andReturn();
        AuthResponse eAuth = objectMapper.readValue(
                objectMapper.readTree(eRes.getResponse().getContentAsString()).get("data").toString(),
                AuthResponse.class
        );
        evaluatorToken = eAuth.getAccessToken();
        evaluatorUserId = eAuth.getUser().getId();

        // 5. Register and Verify University
        RegisterOrganizationRequest orgReq = RegisterOrganizationRequest.builder()
                .name("Indian Institute of Technology (BHU) Varanasi")
                .code("IIT-BHU-" + ts)
                .organizationType(OrganizationType.UNIVERSITY)
                .contactEmail("contact." + ts + "@iitbhu.ac.in")
                .contactPhone("+915422368106")
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
                .andExpect(status().isCreated())
                .andReturn();
        OrganizationResponse orgDto = objectMapper.readValue(
                objectMapper.readTree(orgRes.getResponse().getContentAsString()).get("data").toString(),
                OrganizationResponse.class
        );
        verifiedUnivOrgId = orgDto.getId();

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

        // Add Resource
        InstitutionalResourceRequest resReq = InstitutionalResourceRequest.builder()
                .resourceName("Advanced Ceramic Nanomaterial Laboratory")
                .resourceType(ResourceType.LABORATORY)
                .description("Equipped with high-temperature sintering furnace and ICP-OES spectroscopy")
                .equipmentList("Sintering Kiln, GC-MS, ICP-OES")
                .accessibleToExternalTeams(true)
                .build();
        mockMvc.perform(post("/api/v1/organizations/" + verifiedUnivOrgId + "/resources")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resReq)))
                .andExpect(status().isCreated());

        // Add Faculty Profile
        FacultyProfileRequest fpReq = FacultyProfileRequest.builder()
                .organizationId(verifiedUnivOrgId)
                .departmentName("Department of Chemistry & Materials Engineering")
                .designation("Associate Professor")
                .academicQualification("Ph.D. in Nanotechnology")
                .primaryDiscipline("Nanomaterial Synthesis")
                .researchAreas("Nanomaterial Synthesis, Ceramic Membranes, Water Purification")
                .build();
        mockMvc.perform(post("/api/v1/faculty/profile")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fpReq)))
                .andExpect(status().isOk());

        // Submit Verification Application
        SubmitVerificationRequest verifReq = SubmitVerificationRequest.builder()
                .organizationId(verifiedUnivOrgId)
                .documents(List.of(
                        SupportingDocumentRequest.builder()
                                .documentType(DocumentType.AISHE_CERTIFICATE)
                                .documentName("aishe_cert_2026.pdf")
                                .documentUrl("https://docs.samadhanx.org/proofs/aishe_cert.pdf")
                                .build()
                ))
                .build();
        MvcResult verifRes = mockMvc.perform(post("/api/v1/verifications")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifReq)))
                .andExpect(status().isCreated())
                .andReturn();
        String verifId = objectMapper.readTree(verifRes.getResponse().getContentAsString()).get("data").get("id").asText();

        // Admin Review & Approve Verification
        ReviewVerificationRequest reviewReq = ReviewVerificationRequest.builder()
                .decision(VerificationStatus.VERIFIED)
                .reviewerNotes("AISHE accreditation and NIRF ranking verified via official database.")
                .build();
        mockMvc.perform(post("/api/v1/verifications/" + verifId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk());

        // 6. Submit Citizen Challenge & Escalate to INNOVATION_REQUIRED
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
        ChallengeResponse chDto = objectMapper.readValue(
                objectMapper.readTree(chRes.getResponse().getContentAsString()).get("data").toString(),
                ChallengeResponse.class
        );
        challengeId = chDto.getId();

        // Escalate Challenge to Innovation
        EscalateToInnovationRequest escReq = EscalateToInnovationRequest.builder()
                .escalationJustification("Standard reverse-osmosis solutions waste 70% water as reject brine; novel electricity-free adsorbent membrane required.")
                .build();
        mockMvc.perform(post("/api/v1/challenges/" + challengeId + "/escalate-to-innovation")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(escReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Complete End-to-End Flow: Challenge -> Team Formation -> Solution Proposal -> Scorecard Evaluation -> Shortlist -> Prototyping -> Pilot Ready")
    void completeSolutionDevelopmentLifecycle() throws Exception {
        // Step 1: Student creates multidisciplinary project team with Faculty Mentor invited
        CreateTeamRequest teamReq = CreateTeamRequest.builder()
                .teamName("JalShuddhi Terracotta Innovation Lab")
                .description("Interdisciplinary team combining material scientists and mechanical engineers to build low-cost clay fluoride filters.")
                .challengeId(challengeId)
                .homeUniversityId(verifiedUnivOrgId)
                .initialMembers(List.of(
                        CreateTeamRequest.InitialMemberRequest.builder()
                                .userId(facultyMentorUserId)
                                .universityId(verifiedUnivOrgId)
                                .teamRole(TeamRole.FACULTY_MENTOR)
                                .academicDiscipline("Nanomaterial Chemistry")
                                .invitationNotes("Principal Investigator & Mentor")
                                .build()
                ))
                .build();

        MvcResult teamRes = mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.teamName").value("JalShuddhi Terracotta Innovation Lab"))
                .andReturn();

        TeamResponse teamDto = objectMapper.readValue(
                objectMapper.readTree(teamRes.getResponse().getContentAsString()).get("data").toString(),
                TeamResponse.class
        );
        UUID teamId = teamDto.getId();
        assertNotNull(teamId);

        // Step 2: Faculty Mentor accepts team invitation
        RespondInvitationRequest acceptReq = RespondInvitationRequest.builder().accept(true).build();
        mockMvc.perform(post("/api/v1/teams/" + teamId + "/invitation/respond")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Step 3: Post team discussion update
        PostDiscussionRequest discReq = PostDiscussionRequest.builder()
                .message("Lab preliminary testing of porous clay terracotta candle achieved 98.4% fluoride reduction at zero electricity.")
                .isMentorGuidance(true)
                .build();
        mockMvc.perform(post("/api/v1/teams/" + teamId + "/discussions")
                        .header("Authorization", "Bearer " + facultyMentorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isMentorGuidance").value(true));

        // Step 4: Team submits comprehensive Solution Proposal
        SubmitProposalRequest propReq = SubmitProposalRequest.builder()
                .challengeId(challengeId)
                .teamId(teamId)
                .title("Gravity-Fed Terracotta Hydroxyapatite Nanocomposite Membrane Filter")
                .problemUnderstanding("High fluoride (3.2 mg/L) in groundwater causes severe fluorosis; conventional activated alumina generates toxic sludge.")
                .proposedSolution("We develop a locally sinterable terracotta clay candle infused with biogenic hydroxyapatite and iron nanoparticles.")
                .innovationNovelty("Zero electricity, uses local riverbed clay, 10x cheaper than commercial RO, and produces non-toxic spent cartridges for brick making.")
                .technicalApproach("Stage 1 solar oxidation followed by Stage 2 nano-hydroxyapatite chemisorption column operating under 0.5 bar gravity head.")
                .expectedImpact("Safely provides 15,000 liters of fluoride-safe water daily for 3,500 villagers in Varanasi and Chandauli.")
                .implementationPlan("Month 1: Lab optimization. Month 2-3: Pilot 5 units at village water kiosks. Month 4-6: Gram Panchayat handover.")
                .requiredResources("IIT BHU Ceramic Materials Lab, Sintering Furnaces, Village Testing Hand Pumps")
                .estimatedCostInr(BigDecimal.valueOf(145000.00))
                .scalabilityPlan("Open-source modular design suitable for fabrication by local potter cooperatives.")
                .sustainabilityModel("Community self-sustaining fund of 15 INR per household monthly covers replacement filter candles.")
                .riskMitigation("Flow rate reduction over time prevented via integrated backwash manifold.")
                .prototypeDescription("TRL-4 lab bench prototype demonstrated 0.4 mg/L effluent fluoride from 5.0 mg/L influent.")
                .documents(List.of(
                        ProposalDocumentDto.builder()
                                .documentType(ProposalDocumentType.TECHNICAL_SPEC)
                                .documentName("hydroxyapatite_terracotta_membrane_v1.pdf")
                                .documentUrl("https://media.samadhanx.org/proposals/hydroxyapatite_spec.pdf")
                                .build()
                ))
                .build();

        MvcResult propRes = mockMvc.perform(post("/api/v1/proposals")
                        .header("Authorization", "Bearer " + studentLeadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PROPOSED"))
                .andExpect(jsonPath("$.data.trackingNumber").exists())
                .andReturn();

        ProposalResponse propDto = objectMapper.readValue(
                objectMapper.readTree(propRes.getResponse().getContentAsString()).get("data").toString(),
                ProposalResponse.class
        );
        UUID proposalId = propDto.getId();
        assertNotNull(proposalId);

        // Step 5: External Evaluator submits multi-dimensional scorecard evaluation (0-100)
        EvaluateProposalRequest evalReq = EvaluateProposalRequest.builder()
                .problemUnderstandingScore(92)
                .innovationScore(95)
                .technicalFeasibilityScore(88)
                .socialImpactScore(94)
                .scalabilityScore(90)
                .costEffectivenessScore(92)
                .sustainabilityScore(85)
                .implementationReadinessScore(88)
                .strengths("Remarkable indigenous material utilization, high rural scalability and zero electricity design.")
                .weaknesses("Requires periodic quality monitoring of locally manufactured candles.")
                .qualitativeFeedback("Outstanding solution proposal, highly recommended for prototype grant and field demonstration.")
                .recommendation(EvaluationRecommendation.SHORTLIST)
                .build();

        mockMvc.perform(post("/api/v1/proposals/" + proposalId + "/evaluate")
                        .header("Authorization", "Bearer " + evaluatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.totalScore").exists())
                .andExpect(jsonPath("$.data.scoringRationale").exists());

        // Step 6: Verify Proposal Ranking & Status (Now UNDER_REVIEW)
        mockMvc.perform(get("/api/v1/proposals/challenge/" + challengeId + "/ranked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(proposalId.toString()))
                .andExpect(jsonPath("$.data[0].status").value("UNDER_REVIEW"));

        // Step 7: Admin advances Proposal state: UNDER_REVIEW -> SHORTLISTED -> PROTOTYPING -> PILOT_READY
        ProposalStateUpdateRequest shortlistReq = ProposalStateUpdateRequest.builder()
                .targetStatus(ProposalStatus.SHORTLISTED)
                .notes("Awarded Academic Prototype Grant of 1,45,000 INR")
                .build();
        mockMvc.perform(post("/api/v1/proposals/" + proposalId + "/state")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortlistReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHORTLISTED"))
                .andExpect(jsonPath("$.data.shortlisted").value(true));

        ProposalStateUpdateRequest protoReq = ProposalStateUpdateRequest.builder()
                .targetStatus(ProposalStatus.PROTOTYPING)
                .notes("Fabrication of 5 prototype units commenced in IIT BHU lab")
                .build();
        mockMvc.perform(post("/api/v1/proposals/" + proposalId + "/state")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(protoReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROTOTYPING"));

        ProposalStateUpdateRequest pilotReq = ProposalStateUpdateRequest.builder()
                .targetStatus(ProposalStatus.PILOT_READY)
                .notes("Prototype successfully certified; ready for village pilot deployment")
                .build();
        mockMvc.perform(post("/api/v1/proposals/" + proposalId + "/state")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pilotReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PILOT_READY"));

        // Step 8: Verify Proposal Timeline Audit Events
        mockMvc.perform(get("/api/v1/proposals/" + proposalId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Step 9: Verify Role-Specific Dashboard Metrics
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer " + studentLeadToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.myActiveProjects").value(1));
    }
}
