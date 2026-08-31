package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.TechTransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechTransferRecordRepository extends JpaRepository<TechTransferRecord, UUID> {
    List<TechTransferRecord> findByProposalId(UUID proposalId);
    List<TechTransferRecord> findByReceivingOrganizationId(UUID receivingOrganizationId);
}
