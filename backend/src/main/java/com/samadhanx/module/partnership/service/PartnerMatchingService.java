package com.samadhanx.module.partnership.service;

import com.samadhanx.module.partnership.dto.PartnerCapabilityRequest;
import com.samadhanx.module.partnership.dto.PartnerCapabilityResponse;
import com.samadhanx.module.partnership.dto.PartnerMatchResponse;

import java.util.List;
import java.util.UUID;

public interface PartnerMatchingService {

    PartnerCapabilityResponse registerOrUpdatePartnerCapability(PartnerCapabilityRequest request, UUID userId);

    PartnerCapabilityResponse getPartnerCapability(UUID organizationId);

    List<PartnerMatchResponse> findMatchingPartnersForProposal(UUID proposalId);
}
