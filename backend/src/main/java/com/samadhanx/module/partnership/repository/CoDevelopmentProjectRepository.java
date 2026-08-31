package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.CoDevelopmentProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoDevelopmentProjectRepository extends JpaRepository<CoDevelopmentProject, UUID> {
    List<CoDevelopmentProject> findByProposalId(UUID proposalId);
    List<CoDevelopmentProject> findByPartnerOrganizationId(UUID partnerOrganizationId);
}
