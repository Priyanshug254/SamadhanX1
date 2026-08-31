package com.samadhanx.module.notification.event;

import com.samadhanx.module.notification.entity.enums.NotificationType;
import com.samadhanx.module.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EcosystemEventListener {

    private static final Logger log = LoggerFactory.getLogger(EcosystemEventListener.class);

    private final PushNotificationService pushNotificationService;

    @EventListener
    public void handleEcosystemEvent(EcosystemEvent event) {
        log.info("Received ecosystem event: {} on entity: {}", event.getEventType(), event.getEntityId());

        if (event.getTargetUserId() == null) {
            return;
        }

        NotificationType notifType = mapToNotificationType(event.getEventType());
        String refType = mapToReferenceType(event.getEventType());

        try {
            pushNotificationService.sendNotificationToUser(
                    event.getTargetUserId(),
                    event.getTitle(),
                    event.getMessage(),
                    notifType,
                    event.getEntityId() != null ? event.getEntityId().toString() : "",
                    refType
            );
        } catch (Exception e) {
            log.warn("Failed to dispatch targeted notification for event {}: {}", event.getEventType(), e.getMessage());
        }
    }

    private NotificationType mapToNotificationType(EcosystemEventType eventType) {
        if (eventType == null) return NotificationType.GENERAL;
        return switch (eventType) {
            case CHALLENGE_CREATED -> NotificationType.CHALLENGE_SUBMITTED;
            case CHALLENGE_TRIAGED -> NotificationType.CHALLENGE_TRIAGED;
            case CHALLENGE_RESOLVED -> NotificationType.CHALLENGE_RESOLVED;
            case CHALLENGE_ESCALATED_TO_INNOVATION -> NotificationType.INNOVATION_REQUIRED;
            case PROPOSAL_SUBMITTED, PROPOSAL_STATUS_CHANGED -> NotificationType.PROPOSAL_UPDATE;
            case PILOT_STARTED, PILOT_MILESTONE_REACHED -> NotificationType.PILOT_UPDATE;
            default -> NotificationType.GENERAL;
        };
    }

    private String mapToReferenceType(EcosystemEventType eventType) {
        if (eventType == null) return "CHALLENGE";
        return switch (eventType) {
            case PROPOSAL_SUBMITTED, PROPOSAL_STATUS_CHANGED, EVALUATION_SUBMITTED -> "PROPOSAL";
            case TEAM_CREATED -> "TEAM";
            case PILOT_STARTED, PILOT_MILESTONE_REACHED -> "PILOT";
            case WORK_ITEM_ASSIGNED, WORK_ITEM_OVERDUE -> "WORK_ITEM";
            case APPROVAL_REQUESTED, APPROVAL_COMPLETED -> "APPROVAL";
            default -> "CHALLENGE";
        };
    }
}
