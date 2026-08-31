package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.CollaborationRequest;
import com.samadhanx.module.partnership.entity.enums.CollaborationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CollaborationRequestRepository extends JpaRepository<CollaborationRequest, UUID> {
    List<CollaborationRequest> findByProposalId(UUID proposalId);
    List<CollaborationRequest> findByPartnerOrganizationId(UUID partnerOrganizationId);
    List<CollaborationRequest> findByStatus(CollaborationStatus status);
}
