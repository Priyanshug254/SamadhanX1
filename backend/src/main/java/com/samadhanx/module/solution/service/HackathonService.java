package com.samadhanx.module.solution.service;

import com.samadhanx.module.solution.dto.CreateHackathonRequest;
import com.samadhanx.module.solution.dto.HackathonResponse;
import com.samadhanx.module.solution.entity.enums.HackathonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HackathonService {

    HackathonResponse createHackathon(CreateHackathonRequest request, UUID creatorUserId);

    HackathonResponse getHackathonById(UUID hackathonId);

    HackathonResponse getHackathonByCode(String code);

    Page<HackathonResponse> listHackathons(HackathonStatus status, Pageable pageable);

    void assignEvaluatorToHackathon(UUID hackathonId, UUID evaluatorUserId, String domain, UUID actionByUserId);
}
