package com.salesinsight.infra.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MEETING_CREATED_QUEUE      = "meeting.created";
    public static final String MEETING_TRANSCRIBED_QUEUE  = "meeting.transcribed";
    public static final String MEETING_PROCESSED_QUEUE    = "meeting.processed";

    public static final String MEETING_CREATED_DLQ        = "meeting.created.dlq";
    public static final String MEETING_TRANSCRIBED_DLQ    = "meeting.transcribed.dlq";
    public static final String MEETING_PROCESSED_DLQ      = "meeting.processed.dlq";

    public static final String SALES_INSIGHT_EXCHANGE     = "sales-insight.exchange";
    public static final String DEAD_LETTER_EXCHANGE       = "sales-insight.dlx";

    @Bean
    public DirectExchange salesInsightExchange() {
        return new DirectExchange(SALES_INSIGHT_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue meetingCreatedQueue() {
        return QueueBuilder.durable(MEETING_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MEETING_CREATED_DLQ)
                .build();
    }

    @Bean
    public Queue meetingTranscribedQueue() {
        return QueueBuilder.durable(MEETING_TRANSCRIBED_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MEETING_TRANSCRIBED_DLQ)
                .build();
    }

    @Bean
    public Queue meetingProcessedQueue() {
        return QueueBuilder.durable(MEETING_PROCESSED_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MEETING_PROCESSED_DLQ)
                .build();
    }

    @Bean
    public Queue meetingCreatedDlq() {
        return QueueBuilder.durable(MEETING_CREATED_DLQ).build();
    }

    @Bean
    public Queue meetingTranscribedDlq() {
        return QueueBuilder.durable(MEETING_TRANSCRIBED_DLQ).build();
    }

    @Bean
    public Queue meetingProcessedDlq() {
        return QueueBuilder.durable(MEETING_PROCESSED_DLQ).build();
    }

    @Bean
    public Binding meetingCreatedBinding() {
        return BindingBuilder.bind(meetingCreatedQueue())
                .to(salesInsightExchange())
                .with(MEETING_CREATED_QUEUE);
    }

    @Bean
    public Binding meetingTranscribedBinding() {
        return BindingBuilder.bind(meetingTranscribedQueue())
                .to(salesInsightExchange())
                .with(MEETING_TRANSCRIBED_QUEUE);
    }

    @Bean
    public Binding meetingProcessedBinding() {
        return BindingBuilder.bind(meetingProcessedQueue())
                .to(salesInsightExchange())
                .with(MEETING_PROCESSED_QUEUE);
    }

    @Bean
    public Binding meetingCreatedDlqBinding() {
        return BindingBuilder.bind(meetingCreatedDlq())
                .to(deadLetterExchange())
                .with(MEETING_CREATED_DLQ);
    }

    @Bean
    public Binding meetingTranscribedDlqBinding() {
        return BindingBuilder.bind(meetingTranscribedDlq())
                .to(deadLetterExchange())
                .with(MEETING_TRANSCRIBED_DLQ);
    }

    @Bean
    public Binding meetingProcessedDlqBinding() {
        return BindingBuilder.bind(meetingProcessedDlq())
                .to(deadLetterExchange())
                .with(MEETING_PROCESSED_DLQ);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
