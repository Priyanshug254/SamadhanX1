package com.samadhanx.module.notification;

import com.samadhanx.module.notification.dto.EcosystemActivityFeedItem;
import com.samadhanx.module.notification.entity.NotificationRecord;
import com.samadhanx.module.notification.entity.enums.NotificationType;
import com.samadhanx.module.notification.event.EcosystemEvent;
import com.samadhanx.module.notification.event.EcosystemEventListener;
import com.samadhanx.module.notification.event.EcosystemEventPublisher;
import com.samadhanx.module.notification.event.EcosystemEventType;
import com.samadhanx.module.notification.repository.DeviceTokenRepository;
import com.samadhanx.module.notification.repository.NotificationRecordRepository;
import com.samadhanx.module.notification.service.PushNotificationService;
import com.samadhanx.module.notification.service.PushNotificationServiceImpl;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EcosystemEventTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private PushNotificationService pushNotificationService;
    @Mock
    private DeviceTokenRepository deviceTokenRepository;
    @Mock
    private NotificationRecordRepository notificationRecordRepository;
    @Mock
    private UserRepository userRepository;

    private EcosystemEventPublisher publisher;
    private EcosystemEventListener listener;
    private PushNotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        publisher = new EcosystemEventPublisher(applicationEventPublisher);
        listener = new EcosystemEventListener(pushNotificationService);
        notificationService = new PushNotificationServiceImpl(
                deviceTokenRepository,
                notificationRecordRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("Should publish ecosystem event via Spring ApplicationEventPublisher")
    void testPublishEvent() {
        EcosystemEvent event = EcosystemEvent.builder()
                .eventType(EcosystemEventType.CHALLENGE_ESCALATED_TO_INNOVATION)
                .entityId(UUID.randomUUID())
                .trackingNumber("SMX-2026-0001")
                .title("Arsenic filtration challenge escalated")
                .targetUserId(UUID.randomUUID())
                .build();

        publisher.publishEvent(event);

        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    @DisplayName("Should listen to ecosystem event and route targeted notification")
    void testEventListenerRouting() {
        UUID targetUser = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        EcosystemEvent event = EcosystemEvent.builder()
                .eventType(EcosystemEventType.CHALLENGE_TRIAGED)
                .entityId(entityId)
                .trackingNumber("SMX-2026-0099")
                .title("Challenge Triaged")
                .message("Your reported challenge has been triaged by Municipal Administration")
                .targetUserId(targetUser)
                .build();

        listener.handleEcosystemEvent(event);

        verify(pushNotificationService).sendNotificationToUser(
                eq(targetUser),
                eq("Challenge Triaged"),
                eq("Your reported challenge has been triaged by Municipal Administration"),
                eq(NotificationType.CHALLENGE_TRIAGED),
                eq(entityId.toString()),
                eq("CHALLENGE")
        );
    }

    @Test
    @DisplayName("Should retrieve recent ecosystem activity feed")
    void testGetEcosystemActivityFeed() {
        User user = User.builder().id(UUID.randomUUID()).firstName("Aarav").lastName("Sharma").build();

        NotificationRecord rec = NotificationRecord.builder()
                .id(UUID.randomUUID())
                .user(user)
                .title("Proposal Submitted")
                .body("Team JalShuddhi submitted solution PRP-2026-001")
                .notificationType(NotificationType.PROPOSAL_UPDATE)
                .referenceId("PRP-2026-001")
                .referenceType("PROPOSAL")
                .createdAt(Instant.now())
                .build();

        when(notificationRecordRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(rec)));

        List<EcosystemActivityFeedItem> feed = notificationService.getEcosystemActivityFeed(10);

        assertThat(feed).hasSize(1);
        assertThat(feed.get(0).getTitle()).isEqualTo("Proposal Submitted");
        assertThat(feed.get(0).getReferenceType()).isEqualTo("PROPOSAL");
        assertThat(feed.get(0).getActorName()).isEqualTo("Aarav Sharma");
    }
}
