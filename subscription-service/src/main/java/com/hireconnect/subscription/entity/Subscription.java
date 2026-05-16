package com.hireconnect.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_role", nullable = false)
    private String userRole; // CANDIDATE or RECRUITER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime cancelledAt;

    @Column(nullable = true)
    private String cancellationReason;

    @Column(nullable = true)
    private String paymentMethod;

    @Column(nullable = true)
    private String paymentTransactionId;

    @Column(nullable = true)
    private LocalDateTime lastPaymentAt;

    @Column(nullable = true)
    private LocalDateTime nextBillingAt;

    @Column(nullable = true)
    private Boolean autoRenew = true;

    @Column(name = "usage_limit", nullable = true)
    private Integer usageLimit; // jobPostLimit for Recruiters, applicationLimit for Candidates

    @Column(name = "current_usage", nullable = true)
    private Integer currentUsage;

    @Column(nullable = true)
    private Boolean isTrial = false;

    @Column(nullable = true)
    private LocalDateTime trialEndsAt;
}
