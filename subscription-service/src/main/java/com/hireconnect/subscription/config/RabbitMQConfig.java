package com.hireconnect.subscription.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SUBSCRIPTION_EXCHANGE = "subscription.exchange";
    public static final String SUBSCRIPTION_RENEWAL_QUEUE = "subscription.renewal.queue";
    public static final String SUBSCRIPTION_CANCELLATION_QUEUE = "subscription.cancellation.queue";
    public static final String PAYMENT_SUCCESS_QUEUE = "payment.success.queue";
    public static final String TRIAL_EXPIRED_QUEUE = "trial.expired.queue";

    public static final String INVOICE_EXCHANGE = "invoice.exchange";
    public static final String INVOICE_OVERDUE_QUEUE = "invoice.overdue.queue";
    public static final String PAYMENT_REFUND_QUEUE = "payment.refund.queue";

    @Bean
    public TopicExchange subscriptionExchange() {
        return new TopicExchange(SUBSCRIPTION_EXCHANGE);
    }

    @Bean
    public TopicExchange invoiceExchange() {
        return new TopicExchange(INVOICE_EXCHANGE);
    }

    // Subscription queues
    @Bean
    public Queue subscriptionRenewalQueue() {
        return QueueBuilder.durable(SUBSCRIPTION_RENEWAL_QUEUE).build();
    }

    @Bean
    public Queue subscriptionCancellationQueue() {
        return QueueBuilder.durable(SUBSCRIPTION_CANCELLATION_QUEUE).build();
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE).build();
    }

    @Bean
    public Queue trialExpiredQueue() {
        return QueueBuilder.durable(TRIAL_EXPIRED_QUEUE).build();
    }

    // Invoice queues
    @Bean
    public Queue invoiceOverdueQueue() {
        return QueueBuilder.durable(INVOICE_OVERDUE_QUEUE).build();
    }

    @Bean
    public Queue paymentRefundQueue() {
        return QueueBuilder.durable(PAYMENT_REFUND_QUEUE).build();
    }

    // Subscription bindings
    @Bean
    public Binding subscriptionRenewalBinding() {
        return BindingBuilder.bind(subscriptionRenewalQueue())
                .to(subscriptionExchange())
                .with("subscription.renewal");
    }

    @Bean
    public Binding subscriptionCancellationBinding() {
        return BindingBuilder.bind(subscriptionCancellationQueue())
                .to(subscriptionExchange())
                .with("subscription.cancellation");
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue())
                .to(subscriptionExchange())
                .with("payment.success");
    }

    @Bean
    public Binding trialExpiredBinding() {
        return BindingBuilder.bind(trialExpiredQueue())
                .to(subscriptionExchange())
                .with("trial.expired");
    }

    // Invoice bindings
    @Bean
    public Binding invoiceOverdueBinding() {
        return BindingBuilder.bind(invoiceOverdueQueue())
                .to(invoiceExchange())
                .with("invoice.overdue");
    }

    @Bean
    public Binding paymentRefundBinding() {
        return BindingBuilder.bind(paymentRefundQueue())
                .to(invoiceExchange())
                .with("payment.refund");
    }
}
