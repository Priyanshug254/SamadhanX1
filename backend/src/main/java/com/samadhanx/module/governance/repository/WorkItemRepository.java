package com.samadhanx.module.governance.repository;

import com.samadhanx.module.governance.entity.WorkItem;
import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, UUID> {

    List<WorkItem> findByAssignedToId(UUID userId);

    Page<WorkItem> findByAssignedToId(UUID userId, Pageable pageable);

    List<WorkItem> findByChallengeId(UUID challengeId);

    List<WorkItem> findByProposalId(UUID proposalId);

    List<WorkItem> findByStatus(WorkItemStatus status);

    @Query("SELECT w FROM WorkItem w LEFT JOIN FETCH w.assignedTo LEFT JOIN FETCH w.creatorUser WHERE w.assignedTo.id = :userId AND w.status NOT IN (com.samadhanx.module.governance.entity.enums.WorkItemStatus.COMPLETED, com.samadhanx.module.governance.entity.enums.WorkItemStatus.CANCELLED) ORDER BY w.dueDate ASC")
    List<WorkItem> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT w FROM WorkItem w WHERE w.status NOT IN (com.samadhanx.module.governance.entity.enums.WorkItemStatus.COMPLETED, com.samadhanx.module.governance.entity.enums.WorkItemStatus.CANCELLED) AND w.dueDate < :now")
    List<WorkItem> findOverdueWorkItems(@Param("now") Instant now);

    @Query("SELECT COUNT(w) FROM WorkItem w WHERE w.status NOT IN (com.samadhanx.module.governance.entity.enums.WorkItemStatus.COMPLETED, com.samadhanx.module.governance.entity.enums.WorkItemStatus.CANCELLED) AND w.dueDate < :now")
    long countOverdueWorkItems(@Param("now") Instant now);

    @Query("SELECT COUNT(w) FROM WorkItem w WHERE w.assignedTo.id = :userId AND w.status = :status")
    long countByAssignedToIdAndStatus(@Param("userId") UUID userId, @Param("status") WorkItemStatus status);

    @Query(value = "SELECT w FROM WorkItem w LEFT JOIN FETCH w.assignedTo LEFT JOIN FETCH w.creatorUser WHERE (:status IS NULL OR w.status = :status) AND (:priority IS NULL OR w.priority = :priority) ORDER BY w.createdAt DESC",
           countQuery = "SELECT count(w) FROM WorkItem w WHERE (:status IS NULL OR w.status = :status) AND (:priority IS NULL OR w.priority = :priority)")
    Page<WorkItem> searchWorkItems(
            @Param("status") WorkItemStatus status,
            @Param("priority") WorkItemPriority priority,
            Pageable pageable
    );
}
