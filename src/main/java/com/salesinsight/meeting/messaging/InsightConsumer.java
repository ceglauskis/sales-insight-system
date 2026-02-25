package com.salesinsight.meeting.messaging;

import com.salesinsight.infra.ai.InsightGeneratorService;
import com.salesinsight.infra.messaging.EventPublisher;
import com.salesinsight.infra.messaging.RabbitMQConfig;
import com.salesinsight.meeting.domain.Insight;
import com.salesinsight.meeting.domain.Meeting;
import com.salesinsight.meeting.repository.MeetingRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsightConsumer {

    private final MeetingRepository meetingRepository;
    private final InsightGeneratorService insightGeneratorService;
    private final EventPublisher eventPublisher;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.MEETING_TRANSCRIBED_QUEUE)
    public void consume(String meetingId, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("InsightConsumer recebeu mensagem. meetingId={}", meetingId);
        try {
            Meeting meeting = meetingRepository.findById(UUID.fromString(meetingId))
                    .orElseThrow(() -> new RuntimeException("Meeting não encontrada: " + meetingId));

            Insight insight = insightGeneratorService.generate(meeting);
            meeting.addInsight(insight);
            meeting.markAsProcessed();
            meetingRepository.save(meeting);

            eventPublisher.publishMeetingProcessed(meeting.getId());

            channel.basicAck(deliveryTag, false);
            log.info("Insights gerados com sucesso. meetingId={}", meetingId);

        } catch (Exception e) {
            log.error("Erro ao gerar insights. meetingId={}, erro={}", meetingId, e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}