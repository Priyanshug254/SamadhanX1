package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.ImpactMetric;
import com.samadhanx.module.partnership.entity.enums.KpiName;
import com.samadhanx.module.partnership.entity.enums.MetricVerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactMetricResponse {

    private UUID id;
    private UUID pilotId;
    private String pilotCode;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private KpiName kpiName;
    private BigDecimal baselineValue;
    private BigDecimal targetValue;
    private BigDecimal actualValue;
    private String unitOfMeasure;
    private Instant measurementDate;
    private String evidenceUrl;
    private MetricVerificationStatus verificationStatus;
    private UUID verifiedByUserId;
    private String verifiedByName;
    private Instant verifiedAt;
    private String remarks;
    private Instant createdAt;

    public static ImpactMetricResponse fromEntity(ImpactMetric im) {
        if (im == null) return null;
        return ImpactMetricResponse.builder()
                .id(im.getId())
                .pilotId(im.getPilot() != null ? im.getPilot().getId() : null)
                .pilotCode(im.getPilot() != null ? im.getPilot().getPilotCode() : null)
                .proposalId(im.getProposal() != null ? im.getProposal().getId() : null)
                .proposalTrackingNumber(im.getProposal() != null ? im.getProposal().getTrackingNumber() : null)
                .kpiName(im.getKpiName())
                .baselineValue(im.getBaselineValue())
                .targetValue(im.getTargetValue())
                .actualValue(im.getActualValue())
                .unitOfMeasure(im.getUnitOfMeasure())
                .measurementDate(im.getMeasurementDate())
                .evidenceUrl(im.getEvidenceUrl())
                .verificationStatus(im.getVerificationStatus())
                .verifiedByUserId(im.getVerifiedByUser() != null ? im.getVerifiedByUser().getId() : null)
                .verifiedByName(im.getVerifiedByUser() != null ? im.getVerifiedByUser().getFullName() : null)
                .verifiedAt(im.getVerifiedAt())
                .remarks(im.getRemarks())
                .createdAt(im.getCreatedAt())
                .build();
    }
}
