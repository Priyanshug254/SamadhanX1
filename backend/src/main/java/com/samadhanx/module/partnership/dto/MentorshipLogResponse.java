package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.MentorshipLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorshipLogResponse {

    private UUID id;
    private UUID engagementId;
    private UUID mentorUserId;
    private String mentorName;
    private String sessionTitle;
    private String guidanceNotes;
    private String milestonesReviewed;
    private String actionItems;
    private Instant meetingDate;
    private Instant createdAt;

    public static MentorshipLogResponse fromEntity(MentorshipLog ml) {
        if (ml == null) return null;
        return MentorshipLogResponse.builder()
                .id(ml.getId())
                .engagementId(ml.getEngagement() != null ? ml.getEngagement().getId() : null)
                .mentorUserId(ml.getMentorUser() != null ? ml.getMentorUser().getId() : null)
                .mentorName(ml.getMentorUser() != null ? ml.getMentorUser().getFullName() : null)
                .sessionTitle(ml.getSessionTitle())
                .guidanceNotes(ml.getGuidanceNotes())
                .milestonesReviewed(ml.getMilestonesReviewed())
                .actionItems(ml.getActionItems())
                .meetingDate(ml.getMeetingDate())
                .createdAt(ml.getCreatedAt())
                .build();
    }
}
