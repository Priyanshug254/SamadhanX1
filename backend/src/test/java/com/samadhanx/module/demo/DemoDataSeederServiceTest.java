package com.samadhanx.module.demo;

import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.demo.service.DemoDataSeederServiceImpl;
import com.samadhanx.module.governance.repository.ApprovalRequestRepository;
import com.samadhanx.module.governance.repository.WorkItemRepository;
import com.samadhanx.module.notification.repository.NotificationRecordRepository;
import com.samadhanx.module.organization.repository.DepartmentRepository;
import com.samadhanx.module.organization.repository.DomainRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.partnership.repository.PilotProjectRepository;
import com.samadhanx.module.role.repository.RoleRepository;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.TeamRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import com.samadhanx.module.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private DomainRepository domainRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private PilotProjectRepository pilotProjectRepository;
    @Mock
    private WorkItemRepository workItemRepository;
    @Mock
    private ApprovalRequestRepository approvalRequestRepository;
    @Mock
    private NotificationRecordRepository notificationRecordRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private DemoDataSeederServiceImpl seederService;

    @BeforeEach
    void setUp() {
        seederService = new DemoDataSeederServiceImpl(
                userRepository,
                roleRepository,
                userRoleRepository,
                domainRepository,
                organizationRepository,
                departmentRepository,
                challengeRepository,
                teamRepository,
                proposalRepository,
                pilotProjectRepository,
                workItemRepository,
                approvalRequestRepository,
                notificationRecordRepository,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("Should detect if demo data is already present")
    void testIsDemoDataPresent() {
        when(userRepository.findByEmailIgnoreCase("citizen@samadhanx.org"))
                .thenReturn(Optional.of(User.builder().build()));
        when(challengeRepository.findByTrackingNumber("SMX-2026-08-00101"))
                .thenReturn(Optional.of(Challenge.builder().build()));

        boolean present = seederService.isDemoDataPresent();
        assertThat(present).isTrue();
    }

    @Test
    @DisplayName("Should execute reset and seed without errors")
    void testResetAndSeed() {
        when(passwordEncoder.encode(any())).thenReturn("hashedPass");
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(User.builder().build()));
        when(domainRepository.findByCode(any())).thenReturn(Optional.empty());
        when(domainRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(organizationRepository.findByCodeIgnoreCase(any())).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(departmentRepository.findById(any())).thenReturn(Optional.empty());
        when(departmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(challengeRepository.findByTrackingNumber(any())).thenReturn(Optional.empty());
        when(challengeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(teamRepository.findByChallengeId(any())).thenReturn(java.util.Collections.emptyList());
        when(teamRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(proposalRepository.findByTrackingNumber(any())).thenReturn(Optional.empty());
        when(proposalRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pilotProjectRepository.findByProposalId(any())).thenReturn(java.util.Collections.emptyList());
        when(pilotProjectRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        seederService.resetAndSeedCompleteEcosystem();

        verify(challengeRepository, atLeastOnce()).save(any());
        verify(proposalRepository, atLeastOnce()).save(any());
    }
}
