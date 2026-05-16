package com.hireconnect.interview.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String INTERVIEW_EXCHANGE = "interview.exchange";
    public static final String INTERVIEW_SCHEDULED_QUEUE = "interview.scheduled.queue";
    public static final String INTERVIEW_CONFIRMED_QUEUE = "interview.confirmed.queue";
    public static final String INTERVIEW_RESCHEDULED_QUEUE = "interview.rescheduled.queue";
    public static final String INTERVIEW_CANCELLED_QUEUE = "interview.cancelled.queue";
    public static final String INTERVIEW_UPDATED_QUEUE = "interview.updated.queue";

    @Bean
    public TopicExchange interviewExchange() {
        return new TopicExchange(INTERVIEW_EXCHANGE);
    }

    @Bean
    public Queue interviewScheduledQueue() {
        return QueueBuilder.durable(INTERVIEW_SCHEDULED_QUEUE).build();
    }

    @Bean
    public Queue interviewConfirmedQueue() {
        return QueueBuilder.durable(INTERVIEW_CONFIRMED_QUEUE).build();
    }

    @Bean
    public Queue interviewRescheduledQueue() {
        return QueueBuilder.durable(INTERVIEW_RESCHEDULED_QUEUE).build();
    }

    @Bean
    public Queue interviewCancelledQueue() {
        return QueueBuilder.durable(INTERVIEW_CANCELLED_QUEUE).build();
    }

    @Bean
    public Queue interviewUpdatedQueue() {
        return QueueBuilder.durable(INTERVIEW_UPDATED_QUEUE).build();
    }

    @Bean
    public Binding interviewScheduledBinding() {
        return BindingBuilder.bind(interviewScheduledQueue())
                .to(interviewExchange())
                .with("interview.scheduled");
    }

    @Bean
    public Binding interviewConfirmedBinding() {
        return BindingBuilder.bind(interviewConfirmedQueue())
                .to(interviewExchange())
                .with("interview.confirmed");
    }

    @Bean
    public Binding interviewRescheduledBinding() {
        return BindingBuilder.bind(interviewRescheduledQueue())
                .to(interviewExchange())
                .with("interview.rescheduled");
    }

    @Bean
    public Binding interviewCancelledBinding() {
        return BindingBuilder.bind(interviewCancelledQueue())
                .to(interviewExchange())
                .with("interview.cancelled");
    }

    @Bean
    public Binding interviewUpdatedBinding() {
        return BindingBuilder.bind(interviewUpdatedQueue())
                .to(interviewExchange())
                .with("interview.updated");
    }
}
