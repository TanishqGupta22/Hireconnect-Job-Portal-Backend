package com.hireconnect.profile.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PROFILE_EXCHANGE = "profile.exchange";

    @Bean
    public TopicExchange profileExchange() {
        return new TopicExchange(PROFILE_EXCHANGE);
    }
}
