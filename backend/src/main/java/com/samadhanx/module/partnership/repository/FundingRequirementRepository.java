package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.FundingRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FundingRequirementRepository extends JpaRepository<FundingRequirement, UUID> {
    List<FundingRequirement> findByProposalId(UUID proposalId);
    List<FundingRequirement> findByIsFulfilledFalse();
}
