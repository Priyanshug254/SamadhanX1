package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.ImpactMetric;
import com.samadhanx.module.partnership.entity.enums.KpiName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ImpactMetricRepository extends JpaRepository<ImpactMetric, UUID> {
    List<ImpactMetric> findByProposalId(UUID proposalId);
    List<ImpactMetric> findByPilotId(UUID pilotId);

    @Query("SELECT COALESCE(SUM(im.actualValue), 0) FROM ImpactMetric im WHERE im.kpiName = :kpiName")
    BigDecimal sumActualValueByKpiName(@Param("kpiName") KpiName kpiName);
}
