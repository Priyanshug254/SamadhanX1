package com.samadhanx.module.solution.repository;

import com.samadhanx.module.solution.entity.ProposalTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProposalTimelineEventRepository extends JpaRepository<ProposalTimelineEvent, UUID> {

    @Query("SELECT pte FROM ProposalTimelineEvent pte " +
            "JOIN FETCH pte.actor u " +
            "WHERE pte.proposal.id = :proposalId " +
            "ORDER BY pte.createdAt ASC")
    List<ProposalTimelineEvent> findByProposalIdOrderByCreatedAtAsc(@Param("proposalId") UUID proposalId);
}
