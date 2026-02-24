package com.salesinsight.meeting.messaging;

import com.salesinsight.infra.ai.TranscriptionService;
import com.salesinsight.infra.messaging.EventPublisher;
import com.salesinsight.infra.messaging.RabbitMQConfig;
import com.salesinsight.meeting.domain.Meeting;
import com.salesinsight.meeting.repository.MeetingRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranscriptionConsumer {

    private final MeetingRepository meetingRepository;
    private final TranscriptionService transcriptionService;
    private final EventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMQConfig.MEETING_CREATED_QUEUE)
    public void consume(String meetingId, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("TranscriptionConsumer recebeu mensagem. meetingId={}", meetingId);
        try {
            Meeting meeting = meetingRepository.findById(UUID.fromString(meetingId))
                    .orElseThrow(() -> new RuntimeException("Meeting não encontrada: " + meetingId));

            meeting.markAsProcessing();
            meetingRepository.save(meeting);

            String transcription = transcriptionService.transcribe(meeting.getVideoUrl());

            meeting.saveTranscription(transcription);
            meetingRepository.save(meeting);

            eventPublisher.publishMeetingTranscribed(meeting.getId());

            channel.basicAck(deliveryTag, false);
            log.info("Transcrição concluída. meetingId={}", meetingId);

        } catch (Exception e) {
            log.error("Erro ao transcrever. meetingId={}, erro={}", meetingId, e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}