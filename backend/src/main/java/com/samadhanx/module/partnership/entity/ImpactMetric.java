package com.samadhanx.module.partnership.entity;

import com.samadhanx.module.partnership.entity.enums.KpiName;
import com.samadhanx.module.partnership.entity.enums.MetricVerificationStatus;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "impact_metrics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilot_id")
    private PilotProject pilot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @Enumerated(EnumType.STRING)
    @Column(name = "kpi_name", nullable = false, length = 100)
    private KpiName kpiName;

    @Builder.Default
    @Column(name = "baseline_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal baselineValue = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "target_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal targetValue = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "actual_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal actualValue = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", nullable = false, length = 50)
    private String unitOfMeasure;

    @Builder.Default
    @Column(name = "measurement_date", nullable = false)
    private Instant measurementDate = Instant.now();

    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private MetricVerificationStatus verificationStatus = MetricVerificationStatus.REPORTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedByUser;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImpactMetric that = (ImpactMetric) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
