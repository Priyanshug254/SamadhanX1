package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.CollaborationOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CollaborationOpportunityRepository extends JpaRepository<CollaborationOpportunity, UUID> {
    List<CollaborationOpportunity> findByProposalId(UUID proposalId);
    List<CollaborationOpportunity> findByIsOpenTrue();
}
