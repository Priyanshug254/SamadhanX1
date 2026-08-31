package com.samadhanx.module.governance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedLifecycleTimelineResponse {

    private UUID challengeId;
    private String challengeTrackingNumber;
    private String challengeTitle;
    private String currentStatus;
    private String resolutionPath;
    private String domainName;
    private String assignedDepartment;

    private List<LifecycleStage> stages;
    private List<TimelineItem> auditStream;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LifecycleStage {
        private String stageKey;     // SUBMISSION, AI_TRIAGE, DEPT_RESOLUTION, INNOVATION_RND, INDUSTRY_CSR, PILOT_IMPACT
        private String stageLabel;
        private String status;         // COMPLETED, CURRENT, PENDING, SKIPPED
        private Instant timestamp;
        private String actorRole;
        private String summary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineItem {
        private UUID id;
        private String stage;
        private String action;
        private String fromState;
        private String toState;
        private String actorName;
        private String actorRole;
        private String details;
        private Instant timestamp;
        private boolean isOfficialAction;
    }
}
