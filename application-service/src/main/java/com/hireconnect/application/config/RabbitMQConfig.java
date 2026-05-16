package com.hireconnect.application.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String APPLICATION_EXCHANGE = "application.exchange";
    public static final String APPLICATION_CREATED_QUEUE = "application.created.queue";
    public static final String APPLICATION_STATUS_CHANGED_QUEUE = "application.status.changed.queue";
    public static final String APPLICATION_WITHDRAWN_QUEUE = "application.withdrawn.queue";

    @Bean
    public TopicExchange applicationExchange() {
        return new TopicExchange(APPLICATION_EXCHANGE);
    }

    @Bean
    public Queue applicationCreatedQueue() {
        return QueueBuilder.durable(APPLICATION_CREATED_QUEUE).build();
    }

    @Bean
    public Queue applicationStatusChangedQueue() {
        return QueueBuilder.durable(APPLICATION_STATUS_CHANGED_QUEUE).build();
    }

    @Bean
    public Queue applicationWithdrawnQueue() {
        return QueueBuilder.durable(APPLICATION_WITHDRAWN_QUEUE).build();
    }

    @Bean
    public Binding applicationCreatedBinding() {
        return BindingBuilder.bind(applicationCreatedQueue())
                .to(applicationExchange())
                .with("application.created");
    }

    @Bean
    public Binding applicationStatusChangedBinding() {
        return BindingBuilder.bind(applicationStatusChangedQueue())
                .to(applicationExchange())
                .with("application.status.changed");
    }

    @Bean
    public Binding applicationWithdrawnBinding() {
        return BindingBuilder.bind(applicationWithdrawnQueue())
                .to(applicationExchange())
                .with("application.withdrawn");
    }
}
