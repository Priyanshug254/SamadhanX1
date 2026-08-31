package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.MentorshipEngagement;
import com.samadhanx.module.partnership.entity.enums.MentorshipStatus;
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
public class MentorshipEngagementResponse {

    private UUID id;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private UUID mentorUserId;
    private String mentorName;
    private String mentorEmail;
    private UUID mentorOrganizationId;
    private String mentorOrganizationName;
    private MentorshipStatus mentorshipStatus;
    private String goalsAndObjectives;
    private String invitationNotes;
    private int logCount;
    private Instant createdAt;

    public static MentorshipEngagementResponse fromEntity(MentorshipEngagement me) {
        if (me == null) return null;
        return MentorshipEngagementResponse.builder()
                .id(me.getId())
                .proposalId(me.getProposal() != null ? me.getProposal().getId() : null)
                .proposalTrackingNumber(me.getProposal() != null ? me.getProposal().getTrackingNumber() : null)
                .proposalTitle(me.getProposal() != null ? me.getProposal().getTitle() : null)
                .mentorUserId(me.getMentorUser() != null ? me.getMentorUser().getId() : null)
                .mentorName(me.getMentorUser() != null ? me.getMentorUser().getFullName() : null)
                .mentorEmail(me.getMentorUser() != null ? me.getMentorUser().getEmail() : null)
                .mentorOrganizationId(me.getMentorOrganization() != null ? me.getMentorOrganization().getId() : null)
                .mentorOrganizationName(me.getMentorOrganization() != null ? me.getMentorOrganization().getName() : null)
                .mentorshipStatus(me.getMentorshipStatus())
                .goalsAndObjectives(me.getGoalsAndObjectives())
                .invitationNotes(me.getInvitationNotes())
                .logCount(me.getActivityLogs() != null ? me.getActivityLogs().size() : 0)
                .createdAt(me.getCreatedAt())
                .build();
    }
}
