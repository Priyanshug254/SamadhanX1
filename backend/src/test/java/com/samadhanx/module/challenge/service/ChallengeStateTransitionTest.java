package com.samadhanx.module.challenge.service;

import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.dto.DepartmentActionRequest;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.entity.enums.DepartmentActionType;
import com.samadhanx.module.challenge.repository.ChallengeDepartmentActionRepository;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.challenge.repository.ChallengeTimelineEventRepository;
import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.repository.OrganizationMemberRepository;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
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
@DisplayName("Challenge State Machine & Authorization Unit Tests")
class ChallengeStateTransitionTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeDepartmentActionRepository departmentActionRepository;

    @Mock
    private ChallengeTimelineEventRepository timelineEventRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChallengeServiceImpl challengeService;

    private Challenge challenge;
    private User unauthorizedUser;
    private User superAdmin;

    @BeforeEach
    void setUp() {
        UUID deptOrgId = UUID.randomUUID();
        Organization deptOrg = Organization.builder().id(deptOrgId).name("Public Works Dept").build();
        Department dept = Department.builder().organizationId(deptOrgId).organization(deptOrg).build();

        challenge = Challenge.builder()
                .id(UUID.randomUUID())
                .trackingNumber("SMX-2026-08-11111")
                .title("Road repair needed")
                .status(ChallengeStatus.ROUTED_TO_DEPARTMENT)
                .assignedDepartment(dept)
                .build();

        unauthorizedUser = User.builder()
                .id(UUID.randomUUID())
                .email("citizen@example.com")
                .firstName("Citizen")
                .lastName("User")
                .build();
        unauthorizedUser.addRole(Role.builder().name(RoleName.CITIZEN).build());

        superAdmin = User.builder()
                .id(UUID.randomUUID())
                .email("admin@samadhanx.gov.in")
                .firstName("Admin")
                .lastName("Super")
                .build();
        superAdmin.addRole(Role.builder().name(RoleName.SUPER_ADMIN).build());
    }

    @Test
    @DisplayName("Non-department user without admin role should be forbidden from performing department actions")
    void shouldRejectUnauthorizedDepartmentAction() {
        DepartmentActionRequest req = DepartmentActionRequest.builder()
                .actionType(DepartmentActionType.ACCEPTED_FOR_RESOLUTION)
                .build();

        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(userRepository.findById(unauthorizedUser.getId())).thenReturn(Optional.of(unauthorizedUser));

        assertThrows(ForbiddenException.class, () ->
                challengeService.performDepartmentAction(challenge.getId(), req, unauthorizedUser.getId())
        );
        verify(challengeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Super Admin should be authorized to accept challenge for resolution and advance state machine")
    void shouldAllowSuperAdminDepartmentAction() {
        DepartmentActionRequest req = DepartmentActionRequest.builder()
                .actionType(DepartmentActionType.ACCEPTED_FOR_RESOLUTION)
                .actionNotes("Accepted by central administration")
                .build();

        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(userRepository.findById(superAdmin.getId())).thenReturn(Optional.of(superAdmin));

        challengeService.performDepartmentAction(challenge.getId(), req, superAdmin.getId());

        assertEquals(ChallengeStatus.DEPARTMENT_IN_PROGRESS, challenge.getStatus());
        verify(challengeRepository, times(1)).save(challenge);
        verify(departmentActionRepository, times(1)).save(any());
    }
}
