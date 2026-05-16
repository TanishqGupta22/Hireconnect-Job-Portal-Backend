package com.hireconnect.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_CREATED_QUEUE = "notification.created.queue";
    public static final String NOTIFICATION_READ_QUEUE = "notification.read.queue";
    
    // Application queues
    public static final String APPLICATION_CREATED_QUEUE = "application.created.queue";
    public static final String APPLICATION_STATUS_CHANGED_QUEUE = "application.status.changed.queue";
    
    // Interview queues
    public static final String INTERVIEW_SCHEDULED_QUEUE = "interview.scheduled.queue";
    public static final String INTERVIEW_CONFIRMED_QUEUE = "interview.confirmed.queue";
    public static final String INTERVIEW_RESCHEDULED_QUEUE = "interview.rescheduled.queue";
    public static final String INTERVIEW_CANCELLED_QUEUE = "interview.cancelled.queue";
    
    // Profile queues
    public static final String PROFILE_VIEWED_QUEUE = "profile.viewed.queue";
    
    // Job queues
    public static final String JOB_CREATED_QUEUE = "job.created.queue";
    
    // User queues
    public static final String USER_REGISTERED_QUEUE = "user.registered.queue";
    public static final String USER_PASSWORD_RESET_QUEUE = "user.password.reset.queue";
    
    // Admin queues
    public static final String ADMIN_ALERT_QUEUE = "admin.alert.queue";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange(NOTIFICATION_EXCHANGE).durable(true).build();
    }

    // Notification queues
    @Bean
    public Queue notificationCreatedQueue() {
        return QueueBuilder.durable(NOTIFICATION_CREATED_QUEUE).build();
    }

    @Bean
    public Queue notificationReadQueue() {
        return QueueBuilder.durable(NOTIFICATION_READ_QUEUE).build();
    }

    // Application queues
    @Bean
    public Queue applicationCreatedQueue() {
        return QueueBuilder.durable(APPLICATION_CREATED_QUEUE).build();
    }

    @Bean
    public Queue applicationStatusChangedQueue() {
        return QueueBuilder.durable(APPLICATION_STATUS_CHANGED_QUEUE).build();
    }

    // Interview queues
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
    public Queue profileViewedQueue() {
        return QueueBuilder.durable(PROFILE_VIEWED_QUEUE).build();
    }

    @Bean
    public Queue jobCreatedQueue() {
        return QueueBuilder.durable(JOB_CREATED_QUEUE).build();
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE).build();
    }

    @Bean
    public Queue userPasswordResetQueue() {
        return QueueBuilder.durable(USER_PASSWORD_RESET_QUEUE).build();
    }

    @Bean
    public Queue adminAlertQueue() {
        return QueueBuilder.durable(ADMIN_ALERT_QUEUE).build();
    }

    // Notification bindings
    @Bean
    public Binding notificationCreatedBinding() {
        return BindingBuilder.bind(notificationCreatedQueue())
                .to(notificationExchange())
                .with("notification.created");
    }

    @Bean
    public Binding notificationReadBinding() {
        return BindingBuilder.bind(notificationReadQueue())
                .to(notificationExchange())
                .with("notification.read");
    }

    // Application bindings
    @Bean
    public Binding applicationCreatedBinding() {
        return BindingBuilder.bind(applicationCreatedQueue())
                .to(notificationExchange())
                .with("application.created");
    }

    @Bean
    public Binding applicationStatusChangedBinding() {
        return BindingBuilder.bind(applicationStatusChangedQueue())
                .to(notificationExchange())
                .with("application.status.changed");
    }

    // Interview bindings
    @Bean
    public Binding interviewScheduledBinding() {
        return BindingBuilder.bind(interviewScheduledQueue())
                .to(notificationExchange())
                .with("interview.scheduled");
    }

    @Bean
    public Binding interviewConfirmedBinding() {
        return BindingBuilder.bind(interviewConfirmedQueue())
                .to(notificationExchange())
                .with("interview.confirmed");
    }

    @Bean
    public Binding interviewRescheduledBinding() {
        return BindingBuilder.bind(interviewRescheduledQueue())
                .to(notificationExchange())
                .with("interview.rescheduled");
    }

    @Bean
    public Binding interviewCancelledBinding() {
        return BindingBuilder.bind(interviewCancelledQueue())
                .to(notificationExchange())
                .with("interview.cancelled");
    }

    @Bean
    public Binding profileViewedBinding() {
        return BindingBuilder.bind(profileViewedQueue())
                .to(notificationExchange())
                .with("profile.viewed");
    }

    @Bean
    public Binding jobCreatedBinding() {
        return BindingBuilder.bind(jobCreatedQueue())
                .to(notificationExchange())
                .with("job.created");
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(notificationExchange())
                .with("user.registered");
    }

    @Bean
    public Binding userPasswordResetBinding() {
        return BindingBuilder.bind(userPasswordResetQueue())
                .to(notificationExchange())
                .with("user.password.reset");
    }

    @Bean
    public Binding adminAlertBinding() {
        return BindingBuilder.bind(adminAlertQueue())
                .to(notificationExchange())
                .with("admin.alert");
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMissingQueuesFatal(false);
        factory.setFailedDeclarationRetryInterval(30000L);
        factory.setRecoveryInterval(30000L);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
