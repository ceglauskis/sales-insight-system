package com.salesinsight.meeting.dto;

import com.salesinsight.meeting.domain.Meeting;
import com.salesinsight.meeting.domain.MeetingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        String title,
        MeetingStatus status,
        LocalDateTime createdAt
) {
    public static MeetingResponse from(Meeting meeting) {
        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getStatus(),
                meeting.getCreatedAt()
        );
    }
}