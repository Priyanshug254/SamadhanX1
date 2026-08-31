package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.PilotProject;
import com.samadhanx.module.partnership.entity.enums.PilotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PilotProjectRepository extends JpaRepository<PilotProject, UUID> {
    List<PilotProject> findByProposalId(UUID proposalId);
    Optional<PilotProject> findByPilotCode(String pilotCode);
    List<PilotProject> findByStatus(PilotStatus status);
    long countByStatus(PilotStatus status);

    @Query("SELECT COUNT(DISTINCT p.district) FROM PilotProject p")
    long countDistinctDistrictsCovered();

    @Query("SELECT COALESCE(SUM(p.targetPopulation), 0) FROM PilotProject p")
    long sumTotalTargetPopulation();
}
