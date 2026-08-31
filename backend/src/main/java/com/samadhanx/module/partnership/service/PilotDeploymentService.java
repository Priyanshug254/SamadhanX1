package com.samadhanx.module.partnership.service;

import com.samadhanx.module.partnership.dto.*;

import java.util.List;
import java.util.UUID;

public interface PilotDeploymentService {

    // Validation Testing
    ValidationTestResponse submitValidationTest(SubmitValidationTestRequest request, UUID userId);
    List<ValidationTestResponse> getValidationTestsForProposal(UUID proposalId);
    boolean hasPassedValidation(UUID proposalId);

    // Pilot Projects
    PilotProjectResponse createPilotProject(CreatePilotProjectRequest request, UUID userId);
    PilotProjectResponse updatePilotStatus(UUID pilotId, UpdatePilotStatusRequest request, UUID userId);
    PilotProjectResponse getPilotById(UUID pilotId);
    List<PilotProjectResponse> getPilotsForProposal(UUID proposalId);

    // Impact Measurement
    ImpactMetricResponse recordImpactMetric(RecordImpactMetricRequest request, UUID userId);
    ImpactMetricResponse verifyImpactMetric(UUID metricId, VerifyImpactMetricRequest request, UUID userId);
    List<ImpactMetricResponse> getImpactMetricsForProposal(UUID proposalId);
    ProjectImpactSummaryResponse getProjectImpactSummary(UUID proposalId);

    // Technology Transfer
    TechTransferResponse recordTechTransfer(RecordTechTransferRequest request, UUID userId);
    List<TechTransferResponse> getTechTransfersForProposal(UUID proposalId);

    // Government Oversight Dashboard
    GovernmentOversightDashboardResponse getGovernmentOversightDashboard();
}
