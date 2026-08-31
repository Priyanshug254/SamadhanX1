package com.samadhanx.module.solution.repository;

import com.samadhanx.module.solution.entity.ProjectDiscussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectDiscussionRepository extends JpaRepository<ProjectDiscussion, UUID> {

    @Query("SELECT pd FROM ProjectDiscussion pd " +
            "JOIN FETCH pd.sender u " +
            "WHERE pd.team.id = :teamId " +
            "ORDER BY pd.createdAt ASC")
    List<ProjectDiscussion> findByTeamIdOrderByCreatedAtAsc(@Param("teamId") UUID teamId);

    Page<ProjectDiscussion> findByTeamId(UUID teamId, Pageable pageable);
}
