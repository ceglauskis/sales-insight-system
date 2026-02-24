package com.salesinsight.meeting.messaging;

import com.salesinsight.infra.messaging.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class TranscriptionConsumer {

    @RabbitListener(queues = RabbitMQConfig.MEETING_CREATED_QUEUE)
    public void consume(String meetingId, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("TranscriptionConsumer recebeu mensagem. meetingId={}", meetingId);
        try {
            // TODO: Step 6 — chamar Whisper API aqui
            log.info("Transcrição simulada com sucesso. meetingId={}", meetingId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Erro ao processar transcrição. meetingId={}, erro={}", meetingId, e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}