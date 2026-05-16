package com.hireconnect.subscription.dto;

import com.hireconnect.subscription.entity.SubscriptionPlan;
import com.hireconnect.subscription.entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private Long userId;
    private String userRole;
    private SubscriptionPlan plan;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double amount;
    private SubscriptionStatus status;
    private Integer usageLimit;
    private Integer currentUsage;
}
