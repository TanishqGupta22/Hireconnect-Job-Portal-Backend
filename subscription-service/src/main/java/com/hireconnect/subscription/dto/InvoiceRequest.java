package com.hireconnect.subscription.dto;

import com.hireconnect.subscription.entity.PaymentMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvoiceRequest {
    
    @NotNull(message = "Subscription ID is required")
    private Long subscriptionId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency = "USD";
    
    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;
    
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;
    
    private LocalDateTime dueDate;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 500, message = "Billing address must not exceed 500 characters")
    private String billingAddress;
    
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;
    
    @Size(max = 4, message = "Card last four must not exceed 4 characters")
    private String cardLastFour;
}
