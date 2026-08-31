package com.samadhanx.module.governance;

import com.samadhanx.module.governance.dto.CreateWorkItemRequest;
import com.samadhanx.module.governance.dto.RoleQueueSummaryResponse;
import com.samadhanx.module.governance.dto.UpdateWorkItemRequest;
import com.samadhanx.module.governance.dto.WorkItemResponse;
import com.samadhanx.module.governance.entity.WorkItem;
import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemStatus;
import com.samadhanx.module.governance.entity.enums.WorkItemType;
import com.samadhanx.module.governance.repository.ApprovalRequestRepository;
import com.samadhanx.module.governance.repository.WorkItemRepository;
import com.samadhanx.module.governance.service.WorkItemServiceImpl;
import com.samadhanx.module.notification.service.PushNotificationService;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemServiceTest {

    @Mock
    private WorkItemRepository workItemRepository;
    @Mock
    private ApprovalRequestRepository approvalRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PushNotificationService pushNotificationService;

    private WorkItemServiceImpl workItemService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        workItemService = new WorkItemServiceImpl(workItemRepository, approvalRequestRepository, userRepository, pushNotificationService);
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .email("official@samadhanx.gov.in")
                .firstName("Rajesh")
                .lastName("Officer")
                .build();
    }

    @Test
    @DisplayName("Should create work item and assign to user with notification")
    void testCreateWorkItem() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        WorkItem saved = WorkItem.builder()
                .id(UUID.randomUUID())
                .title("Triage Water Pipeline Issue")
                .itemType(WorkItemType.CHALLENGE_TRIAGE)
                .status(WorkItemStatus.TODO)
                .priority(WorkItemPriority.HIGH)
                .assignedTo(testUser)
                .creatorUser(testUser)
                .build();

        when(workItemRepository.save(any())).thenReturn(saved);

        CreateWorkItemRequest request = CreateWorkItemRequest.builder()
                .title("Triage Water Pipeline Issue")
                .itemType(WorkItemType.CHALLENGE_TRIAGE)
                .priority(WorkItemPriority.HIGH)
                .assignedToUserId(userId)
                .build();

        WorkItemResponse response = workItemService.createWorkItem(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Triage Water Pipeline Issue");
        assertThat(response.getPriority()).isEqualTo(WorkItemPriority.HIGH);
        verify(pushNotificationService).sendNotificationToUser(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should update work item status and record completed timestamp")
    void testUpdateWorkItemCompletion() {
        UUID itemId = UUID.randomUUID();
        WorkItem existing = WorkItem.builder()
                .id(itemId)
                .title("Verify Handpump Water Test")
                .status(WorkItemStatus.IN_PROGRESS)
                .build();

        when(workItemRepository.findById(itemId)).thenReturn(Optional.of(existing));
        when(workItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateWorkItemRequest request = UpdateWorkItemRequest.builder()
                .status(WorkItemStatus.COMPLETED)
                .resolutionNotes("Fluoride levels within standard limits after filter installation")
                .build();

        WorkItemResponse response = workItemService.updateWorkItem(itemId, request, userId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(WorkItemStatus.COMPLETED);
        assertThat(response.getCompletedAt()).isNotNull();
        assertThat(response.getResolutionNotes()).contains("Fluoride levels");
    }

    @Test
    @DisplayName("Should generate role queue summary with active and overdue tasks")
    void testRoleQueueSummary() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        WorkItem item = WorkItem.builder()
                .id(UUID.randomUUID())
                .title("Critical Bridge Repair Inspection")
                .priority(WorkItemPriority.CRITICAL)
                .status(WorkItemStatus.IN_PROGRESS)
                .dueDate(Instant.now().minus(2, ChronoUnit.DAYS))
                .build();

        when(workItemRepository.findActiveByUserId(userId)).thenReturn(List.of(item));
        when(workItemRepository.countOverdueWorkItems(any())).thenReturn(1L);
        when(approvalRequestRepository.countPendingApprovals()).thenReturn(2L);

        RoleQueueSummaryResponse summary = workItemService.getRoleQueueSummary(userId);

        assertThat(summary).isNotNull();
        assertThat(summary.getMyActiveTasksCount()).isEqualTo(1);
        assertThat(summary.getOverdueWorkItemsCount()).isEqualTo(1);
        assertThat(summary.getPendingApprovalsCount()).isEqualTo(2);
        assertThat(summary.getHighPriorityWorkItems()).hasSize(1);
    }
}
