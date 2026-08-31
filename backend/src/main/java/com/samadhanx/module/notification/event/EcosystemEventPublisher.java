package com.samadhanx.module.notification.event;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EcosystemEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EcosystemEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public void publishEvent(EcosystemEvent event) {
        if (event == null) return;
        log.info("Publishing ecosystem event: {} for entity: {} [{}]",
                event.getEventType(), event.getEntityId(), event.getTrackingNumber());
        eventPublisher.publishEvent(event);
    }
}
