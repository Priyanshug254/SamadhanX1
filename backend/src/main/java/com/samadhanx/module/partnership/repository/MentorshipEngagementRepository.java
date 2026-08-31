package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.MentorshipEngagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorshipEngagementRepository extends JpaRepository<MentorshipEngagement, UUID> {
    List<MentorshipEngagement> findByProposalId(UUID proposalId);
    List<MentorshipEngagement> findByMentorUserId(UUID mentorUserId);
}
