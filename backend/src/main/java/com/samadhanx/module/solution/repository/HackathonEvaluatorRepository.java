package com.samadhanx.module.solution.repository;

import com.samadhanx.module.solution.entity.HackathonEvaluator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HackathonEvaluatorRepository extends JpaRepository<HackathonEvaluator, UUID> {
    List<HackathonEvaluator> findByHackathonId(UUID hackathonId);
    boolean existsByHackathonIdAndEvaluatorId(UUID hackathonId, UUID evaluatorId);
}
