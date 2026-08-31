package com.samadhanx.module.governance.repository;

import com.samadhanx.module.governance.entity.ApprovalRequest;
import com.samadhanx.module.governance.entity.enums.ApprovalStatus;
import com.samadhanx.module.governance.entity.enums.WorkflowActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {

    List<ApprovalRequest> findByTargetEntityId(UUID targetEntityId);

    List<ApprovalRequest> findByStatus(ApprovalStatus status);

    Page<ApprovalRequest> findByStatus(ApprovalStatus status, Pageable pageable);

    @Query("SELECT a FROM ApprovalRequest a WHERE a.status = 'PENDING' ORDER BY a.createdAt ASC")
    List<ApprovalRequest> findPendingApprovals();

    @Query("SELECT COUNT(a) FROM ApprovalRequest a WHERE a.status = 'PENDING'")
    long countPendingApprovals();

    Optional<ApprovalRequest> findByTargetEntityIdAndWorkflowTypeAndStatus(
            UUID targetEntityId,
            WorkflowActionType workflowType,
            ApprovalStatus status
    );
}
