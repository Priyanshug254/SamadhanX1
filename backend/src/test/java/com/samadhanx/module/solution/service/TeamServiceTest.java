package com.samadhanx.module.solution.service;

import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.OrganizationMemberRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.dto.CreateTeamRequest;
import com.samadhanx.module.solution.dto.InviteMemberRequest;
import com.samadhanx.module.solution.dto.TeamMemberDto;
import com.samadhanx.module.solution.dto.TeamResponse;
import com.samadhanx.module.solution.entity.Team;
import com.samadhanx.module.solution.entity.TeamMember;
import com.samadhanx.module.solution.entity.enums.TeamMemberStatus;
import com.samadhanx.module.solution.entity.enums.TeamRole;
import com.samadhanx.module.solution.entity.enums.TeamStatus;
import com.samadhanx.module.solution.repository.TeamMemberRepository;
import com.samadhanx.module.solution.repository.TeamRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService Unit Tests")
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private User studentLead;
    private User facultyMentor;
    private Organization university;
    private Challenge challenge;

    @BeforeEach
    void setUp() {
        studentLead = User.builder()
                .id(UUID.randomUUID())
                .email("rahul.student@iitbhu.ac.in")
                .firstName("Rahul")
                .lastName("Verma")
                .build();
        studentLead.addRole(Role.builder().name(RoleName.STUDENT).build());

        facultyMentor = User.builder()
                .id(UUID.randomUUID())
                .email("prof.sharma@iitbhu.ac.in")
                .firstName("Anil")
                .lastName("Sharma")
                .build();
        facultyMentor.addRole(Role.builder().name(RoleName.FACULTY).build());

        university = Organization.builder()
                .id(UUID.randomUUID())
                .name("IIT BHU")
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        challenge = Challenge.builder()
                .id(UUID.randomUUID())
                .title("Arsenic remediation in rural water")
                .trackingNumber("SMX-2026-08-99999")
                .status(ChallengeStatus.INNOVATION_REQUIRED)
                .build();
    }

    @Test
    @DisplayName("Should create multidisciplinary team with creator as ACTIVE TEAM_LEAD")
    void shouldCreateTeamSuccessfully() {
        CreateTeamRequest req = CreateTeamRequest.builder()
                .teamName("EcoJal Nanotech Research Team")
                .description("Terracotta nanomembrane filtration R&D team")
                .challengeId(challenge.getId())
                .homeUniversityId(university.getId())
                .build();

        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(organizationRepository.findById(university.getId())).thenReturn(Optional.of(university));
        when(userRepository.findById(studentLead.getId())).thenReturn(Optional.of(studentLead));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TeamResponse response = teamService.createTeam(req, studentLead.getId());

        assertNotNull(response);
        assertEquals("EcoJal Nanotech Research Team", response.getTeamName());
        assertEquals(studentLead.getId(), response.getCreatedById());
        verify(teamMemberRepository, times(1)).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("Should invite faculty mentor and allow acceptance")
    void shouldInviteAndAcceptMember() {
        Team team = Team.builder()
                .id(UUID.randomUUID())
                .teamName("EcoJal Team")
                .createdBy(studentLead)
                .homeUniversity(university)
                .status(TeamStatus.FORMING)
                .build();

        InviteMemberRequest inviteReq = InviteMemberRequest.builder()
                .userId(facultyMentor.getId())
                .universityId(university.getId())
                .teamRole(TeamRole.FACULTY_MENTOR)
                .academicDiscipline("Nanomaterial Chemistry")
                .invitationNotes("Requested as Principal Mentor")
                .build();

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(studentLead.getId())).thenReturn(Optional.of(studentLead));
        when(userRepository.findById(facultyMentor.getId())).thenReturn(Optional.of(facultyMentor));
        when(organizationRepository.findById(university.getId())).thenReturn(Optional.of(university));
        when(teamMemberRepository.existsByTeamIdAndUserId(team.getId(), facultyMentor.getId())).thenReturn(false);
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamMemberDto invited = teamService.inviteMember(team.getId(), inviteReq, studentLead.getId());

        assertNotNull(invited);
        assertEquals(TeamRole.FACULTY_MENTOR, invited.getTeamRole());
        assertEquals(TeamMemberStatus.INVITED, invited.getStatus());

        // Now Faculty Accepts Invitation
        TeamMember pendingMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .team(team)
                .user(facultyMentor)
                .university(university)
                .teamRole(TeamRole.FACULTY_MENTOR)
                .status(TeamMemberStatus.INVITED)
                .build();

        when(teamMemberRepository.findByTeamIdAndUserId(team.getId(), facultyMentor.getId()))
                .thenReturn(Optional.of(pendingMember));

        TeamMemberDto accepted = teamService.respondToInvitation(team.getId(), true, facultyMentor.getId());

        assertNotNull(accepted);
        assertEquals(TeamMemberStatus.ACTIVE, accepted.getStatus());
        assertEquals(TeamStatus.ACTIVE, team.getStatus());
    }

    @Test
    @DisplayName("Should prevent non-member from inviting or modifying team")
    void shouldRejectUnauthorizedInvitation() {
        User outsider = User.builder().id(UUID.randomUUID()).email("outsider@other.edu").build();
        outsider.addRole(Role.builder().name(RoleName.STUDENT).build());

        Team team = Team.builder()
                .id(UUID.randomUUID())
                .teamName("EcoJal Team")
                .createdBy(studentLead)
                .homeUniversity(university)
                .build();

        InviteMemberRequest inviteReq = InviteMemberRequest.builder()
                .userId(facultyMentor.getId())
                .universityId(university.getId())
                .teamRole(TeamRole.STUDENT)
                .build();

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(outsider.getId())).thenReturn(Optional.of(outsider));

        assertThrows(ForbiddenException.class, () ->
                teamService.inviteMember(team.getId(), inviteReq, outsider.getId())
        );
    }
}
