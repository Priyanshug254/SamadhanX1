package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.CoDevMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoDevMilestoneRepository extends JpaRepository<CoDevMilestone, UUID> {
    List<CoDevMilestone> findByProjectId(UUID projectId);
}
