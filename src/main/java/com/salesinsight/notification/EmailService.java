package com.salesinsight.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendInsightsReady(String to, String meetingTitle, String meetingId) {
        log.info("Enviando email de notificação. to={}, meetingId={}", to, meetingId);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("✅ Insights prontos: " + meetingTitle);
        message.setText("""
                Olá!
                
                Os insights da sua reunião "%s" foram gerados com sucesso.
                
                Acesse pelo link:
                http://localhost:8080/meetings/%s/insights
                
                Abraços,
                Sales Insight
                """.formatted(meetingTitle, meetingId));

        mailSender.send(message);
        log.info("Email enviado com sucesso. to={}", to);
    }
}
