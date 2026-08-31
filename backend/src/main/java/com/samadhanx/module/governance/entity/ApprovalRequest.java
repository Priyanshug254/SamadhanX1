package com.samadhanx.module.governance.entity;

import com.samadhanx.common.entity.BaseAuditEntity;
import com.samadhanx.module.governance.entity.enums.ApprovalStatus;
import com.samadhanx.module.governance.entity.enums.WorkflowActionType;
import com.samadhanx.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_requests", indexes = {
        @Index(name = "idx_approval_status", columnList = "status"),
        @Index(name = "idx_approval_target_id", columnList = "target_entity_id"),
        @Index(name = "idx_approval_workflow_type", columnList = "workflow_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false, length = 50)
    private WorkflowActionType workflowType;

    @Column(name = "target_entity_id", nullable = false)
    private UUID targetEntityId;

    @Column(name = "target_reference_code", length = 50)
    private String targetReferenceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;

    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "previous_state", length = 50)
    private String previousState;

    @Column(name = "target_state", length = 50)
    private String targetState;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;
}
