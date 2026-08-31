package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.FundingOffer;
import com.samadhanx.module.partnership.entity.enums.FundingOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface FundingOfferRepository extends JpaRepository<FundingOffer, UUID> {
    List<FundingOffer> findByRequirementId(UUID requirementId);
    List<FundingOffer> findByProposalId(UUID proposalId);
    List<FundingOffer> findBySponsorOrganizationId(UUID sponsorOrganizationId);
    List<FundingOffer> findByStatus(FundingOfferStatus status);

    @Query("SELECT COALESCE(SUM(fo.disbursedAmountInr), 0) FROM FundingOffer fo WHERE fo.status IN ('APPROVED', 'DISBURSED', 'UTILIZED', 'CLOSED')")
    BigDecimal sumTotalApprovedFunding();
}
