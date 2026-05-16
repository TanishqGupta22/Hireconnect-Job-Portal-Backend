package com.hireconnect.subscription.dto;

import com.hireconnect.subscription.entity.InvoiceStatus;
import com.hireconnect.subscription.entity.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InvoiceResponse {
    private Long id;
    private Long subscriptionId;
    private Double amount;
    private String currency;
    private PaymentMode paymentMode;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private InvoiceStatus status;
    private LocalDateTime dueDate;
    private String description;
    private String billingAddress;
    private String paymentMethod;
    private String cardLastFour;
    private String receiptUrl;
    private String refundReason;
    private LocalDateTime refundedAt;
    private Double refundedAmount;
}
