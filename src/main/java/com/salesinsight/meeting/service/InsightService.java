package com.salesinsight.meeting.service;

import com.salesinsight.meeting.domain.Meeting;
import com.salesinsight.meeting.dto.InsightResponse;
import com.salesinsight.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {

    private final MeetingRepository meetingRepository;

    @Cacheable(value = "insights", key = "#meetingId")
    public List<InsightResponse> findByMeetingId(UUID meetingId, UUID ownerId) {
        log.info("Buscando insights. meetingId={}", meetingId);

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting não encontrada: " + meetingId));

        if (!meeting.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Meeting não encontrada: " + meetingId);
        }

        return meeting.getInsights()
                .stream()
                .map(InsightResponse::from)
                .toList();
    }
}
