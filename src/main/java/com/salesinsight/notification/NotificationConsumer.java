package com.salesinsight.notification;

import com.salesinsight.infra.messaging.RabbitMQConfig;
import com.salesinsight.user.domain.User;
import com.salesinsight.user.repository.UserRepository;
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
public class NotificationConsumer {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.MEETING_PROCESSED_QUEUE)
    public void consume(String meetingId, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("NotificationConsumer recebeu mensagem. meetingId={}", meetingId);
        try {
            Meeting meeting = meetingRepository.findById(UUID.fromString(meetingId))
                    .orElseThrow(() -> new RuntimeException("Meeting não encontrada: " + meetingId));

            User user = userRepository.findById(meeting.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + meeting.getOwnerId()));

            emailService.sendInsightsReady(
                    user.getEmail(),
                    meeting.getTitle(),
                    meeting.getId().toString()
            );

            channel.basicAck(deliveryTag, false);
            log.info("Notificação enviada com sucesso. meetingId={}", meetingId);

        } catch (Exception e) {
            log.error("Erro ao enviar notificação. meetingId={}, erro={}", meetingId, e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}