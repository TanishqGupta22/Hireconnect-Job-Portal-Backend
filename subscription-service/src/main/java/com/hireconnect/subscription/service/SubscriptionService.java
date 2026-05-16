package com.hireconnect.subscription.service;

import com.hireconnect.subscription.dto.SubscriptionRequest;
import com.hireconnect.subscription.dto.SubscriptionResponse;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.entity.SubscriptionPlan;
import com.hireconnect.subscription.entity.SubscriptionStatus;
import com.hireconnect.subscription.exception.SubscriptionNotFoundException;
import com.hireconnect.subscription.repository.SubscriptionRepository;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final RazorpayService razorpayService;
    private final RabbitTemplate rabbitTemplate;

    public String initiateSubscription(Long userId, String userRole, SubscriptionPlan plan) throws RazorpayException {
        Double amount = getPlanPrice(plan);
        String receipt = "receipt_" + userId + "_" + System.currentTimeMillis();
        return razorpayService.createOrder(amount, "INR", receipt);
    }

    @Transactional
    public SubscriptionResponse completeSubscription(SubscriptionRequest request) {
        // Verify Razorpay signature (skip for FREE plan)
        if (!"FREE".equals(request.getRazorpayOrderId())) {
            boolean isValid = razorpayService.verifyPaymentSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );

            if (!isValid) {
                throw new IllegalArgumentException("Invalid payment signature");
            }
        }

        // Deactivate old subscription if any
        Optional<Subscription> oldSub = subscriptionRepository.findByUserIdAndUserRole(request.getUserId(), request.getUserRole());
        oldSub.ifPresent(sub -> {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
        });

        Subscription subscription = Subscription.builder()
                .userId(request.getUserId())
                .userRole(request.getUserRole())
                .plan(request.getPlan())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .amount(getPlanPrice(request.getPlan()))
                .status(SubscriptionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .paymentMethod("RAZORPAY")
                .paymentTransactionId(request.getRazorpayPaymentId())
                .autoRenew(true)
                .usageLimit(getUsageLimit(request.getPlan()))
                .currentUsage(0)
                .build();

        subscription = subscriptionRepository.save(subscription);

        // Send event
        sendSubscriptionEvent("payment.success", subscription);

        return mapToResponse(subscription);
    }

    public boolean canApplyOrPost(Long userId, String userRole) {
        Subscription sub = subscriptionRepository.findByUserIdAndUserRole(userId, userRole)
                .orElseGet(() -> createFreeSubscription(userId, userRole));

        if (sub.getStatus() != SubscriptionStatus.ACTIVE && sub.getPlan() != SubscriptionPlan.FREE) {
            return false;
        }

        if (sub.getUsageLimit() == -1) {
            return true; // Unlimited
        }

        return sub.getCurrentUsage() < sub.getUsageLimit();
    }

    @Transactional
    public void incrementUsage(Long userId, String userRole) {
        Subscription sub = subscriptionRepository.findByUserIdAndUserRole(userId, userRole)
                .orElseGet(() -> createFreeSubscription(userId, userRole));
        
        sub.setCurrentUsage(sub.getCurrentUsage() + 1);
        subscriptionRepository.save(sub);
    }

    private Subscription createFreeSubscription(Long userId, String userRole) {
        Subscription sub = Subscription.builder()
                .userId(userId)
                .userRole(userRole)
                .plan(SubscriptionPlan.FREE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusYears(10))
                .amount(0.0)
                .status(SubscriptionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageLimit(10)
                .currentUsage(0)
                .build();
        return subscriptionRepository.save(sub);
    }

    private Double getPlanPrice(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> 0.0;
            case BASIC -> 49.0;
            case PRO -> 99.0;
            case PREMIUM -> 199.0;
        };
    }

    private Integer getUsageLimit(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> 10;
            case BASIC -> 20;
            case PRO -> 50;
            case PREMIUM -> -1; // Unlimited
        };
    }

    public SubscriptionResponse getCurrentSubscription(Long userId, String userRole) {
        Subscription sub = subscriptionRepository.findByUserIdAndUserRole(userId, userRole)
                .orElseGet(() -> createFreeSubscription(userId, userRole));
        return mapToResponse(sub);
    }

    private void sendSubscriptionEvent(String eventType, Subscription subscription) {
        try {
            rabbitTemplate.convertAndSend("subscription.exchange", "subscription." + eventType, subscription);
        } catch (Exception e) {
            log.error("Failed to send subscription event", e);
        }
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getUserRole(),
                subscription.getPlan(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getAmount(),
                subscription.getStatus(),
                subscription.getUsageLimit(),
                subscription.getCurrentUsage()
        );
    }
}
