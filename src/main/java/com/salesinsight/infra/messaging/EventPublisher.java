package com.salesinsight.infra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishMeetingCreated(UUID meetingId) {
        log.info("Publicando MeetingCreatedEvent. meetingId={}", meetingId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SALES_INSIGHT_EXCHANGE,
                RabbitMQConfig.MEETING_CREATED_QUEUE,
                meetingId.toString()
        );
    }

    public void publishMeetingTranscribed(UUID meetingId) {
        log.info("Publicando MeetingTranscribedEvent. meetingId={}", meetingId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SALES_INSIGHT_EXCHANGE,
                RabbitMQConfig.MEETING_TRANSCRIBED_QUEUE,
                meetingId.toString()
        );
    }

    public void publishMeetingProcessed(UUID meetingId) {
        log.info("Publicando MeetingProcessedEvent. meetingId={}", meetingId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SALES_INSIGHT_EXCHANGE,
                RabbitMQConfig.MEETING_PROCESSED_QUEUE,
                meetingId.toString()
        );
    }
}