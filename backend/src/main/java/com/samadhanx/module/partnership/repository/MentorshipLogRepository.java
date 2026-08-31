package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.MentorshipLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorshipLogRepository extends JpaRepository<MentorshipLog, UUID> {
    List<MentorshipLog> findByEngagementIdOrderByMeetingDateDesc(UUID engagementId);
}
