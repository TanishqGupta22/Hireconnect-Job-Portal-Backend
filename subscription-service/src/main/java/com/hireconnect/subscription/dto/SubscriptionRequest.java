package com.hireconnect.subscription.dto;

import com.hireconnect.subscription.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "User Role is required")
    private String userRole;
    
    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan plan;
    
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private String paymentMethod;
    
    private String paymentTransactionId;
    
    private Boolean autoRenew = true;
    
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
