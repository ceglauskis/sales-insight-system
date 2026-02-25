package com.salesinsight.meeting.service;

import com.salesinsight.infra.messaging.EventPublisher;
import com.salesinsight.infra.storage.FileStorageService;
import com.salesinsight.meeting.domain.Meeting;
import com.salesinsight.meeting.dto.InsightResponse;
import com.salesinsight.meeting.dto.MeetingResponse;
import com.salesinsight.meeting.dto.MeetingUploadRequest;
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
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final FileStorageService fileStorageService;
    private final EventPublisher eventPublisher;

    public MeetingResponse upload(MeetingUploadRequest request, UUID ownerId) {
        log.info("Iniciando upload de meeting. título={}, ownerId={}", request.title(), ownerId);

        String videoUrl = fileStorageService.store(request.file());
        log.debug("Arquivo salvo em: {}", videoUrl);

        Meeting meeting = new Meeting(request.title(), videoUrl, ownerId);
        meetingRepository.save(meeting);

        eventPublisher.publishMeetingCreated(meeting.getId());

        log.info("Meeting criada com sucesso. meetingId={}", meeting.getId());

        return MeetingResponse.from(meeting);
    }

    public MeetingResponse findById(UUID meetingId, UUID ownerId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting não encontrada: " + meetingId));

        if (!meeting.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Meeting não encontrada: " + meetingId);
        }

        return MeetingResponse.from(meeting);
    }

    @Cacheable(value = "insights", key = "#meetingId")
    public List<InsightResponse> findInsights(UUID meetingId, UUID ownerId) {
        log.info("Buscando insights. meetingId={}", meetingId);

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting não encontrada: " + meetingId));

        if (!meeting.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Meeting não encontrada: " + meetingId);
        }

        return meeting.getInsights().stream()
                .map(InsightResponse::from)
                .toList();
    }
}