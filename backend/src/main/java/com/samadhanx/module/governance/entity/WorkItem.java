package com.samadhanx.module.governance.entity;

import com.samadhanx.common.entity.BaseAuditEntity;
import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemStatus;
import com.samadhanx.module.governance.entity.enums.WorkItemType;
import com.samadhanx.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "work_items", indexes = {
        @Index(name = "idx_workitem_assigned_to", columnList = "assigned_to_user_id"),
        @Index(name = "idx_workitem_status", columnList = "status"),
        @Index(name = "idx_workitem_challenge_id", columnList = "challenge_id"),
        @Index(name = "idx_workitem_proposal_id", columnList = "proposal_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 40)
    private WorkItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private WorkItemStatus status = WorkItemStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private WorkItemPriority priority = WorkItemPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id")
    private User creatorUser;

    @Column(name = "challenge_id")
    private UUID challengeId;

    @Column(name = "challenge_tracking_number", length = 50)
    private String challengeTrackingNumber;

    @Column(name = "proposal_id")
    private UUID proposalId;

    @Column(name = "proposal_tracking_number", length = 50)
    private String proposalTrackingNumber;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "pilot_id")
    private UUID pilotId;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    public boolean isOverdue() {
        return dueDate != null && status != WorkItemStatus.COMPLETED && status != WorkItemStatus.CANCELLED && Instant.now().isAfter(dueDate);
    }
}
