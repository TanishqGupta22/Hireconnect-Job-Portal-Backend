package com.hireconnect.subscription.service;

import com.hireconnect.subscription.dto.InvoiceRequest;
import com.hireconnect.subscription.dto.InvoiceResponse;
import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.InvoiceStatus;
import com.hireconnect.subscription.exception.InvoiceNotFoundException;
import com.hireconnect.subscription.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        Invoice invoice = Invoice.builder()
                .subscriptionId(request.getSubscriptionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMode(request.getPaymentMode())
                .transactionId(request.getTransactionId() != null ? request.getTransactionId() : UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .status(InvoiceStatus.PENDING)
                .dueDate(request.getDueDate() != null ? request.getDueDate() : LocalDateTime.now().plusDays(7))
                .description(request.getDescription())
                .billingAddress(request.getBillingAddress())
                .paymentMethod(request.getPaymentMethod())
                .cardLastFour(request.getCardLastFour())
                .build();

        invoice = invoiceRepository.save(invoice);

        log.info("Created invoice {} for subscription ID: {}", invoice.getId(), request.getSubscriptionId());

        return mapToResponse(invoice);
    }

    @Transactional
    public InvoiceResponse markAsPaid(Long id, String transactionId, String receiptUrl) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + id));

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setTransactionId(transactionId);
        invoice.setReceiptUrl(receiptUrl);

        invoice = invoiceRepository.save(invoice);

        // Send payment success event
        sendInvoiceEvent("payment.success", invoice);

        log.info("Marked invoice {} as paid", id);

        return mapToResponse(invoice);
    }

    @Transactional
    public InvoiceResponse markAsFailed(Long id, String reason) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + id));

        invoice.setStatus(InvoiceStatus.FAILED);

        invoice = invoiceRepository.save(invoice);

        log.info("Marked invoice {} as failed. Reason: {}", id, reason);

        return mapToResponse(invoice);
    }

    @Transactional
    public InvoiceResponse refundInvoice(Long id, Double amount, String reason) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + id));

        if (invoice.getStatus() != InvoiceStatus.PAID) {
            throw new IllegalArgumentException("Can only refund paid invoices");
        }

        invoice.setStatus(InvoiceStatus.REFUNDED);
        invoice.setRefundedAmount(amount);
        invoice.setRefundReason(reason);
        invoice.setRefundedAt(LocalDateTime.now());

        invoice = invoiceRepository.save(invoice);

        // Send refund event
        sendInvoiceEvent("payment.refund", invoice);

        log.info("Refunded invoice {} with amount {}. Reason: {}", id, amount, reason);

        return mapToResponse(invoice);
    }

    @Transactional
    public void cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalArgumentException("Cannot cancel paid invoices");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);

        log.info("Cancelled invoice {}", id);
    }

    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + id));

        return mapToResponse(invoice);
    }

    public Page<InvoiceResponse> getInvoicesBySubscription(Long subscriptionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invoice> invoices = invoiceRepository.findBySubscriptionId(subscriptionId, pageable);

        return invoices.map(this::mapToResponse);
    }

    public Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invoice> invoices = invoiceRepository.findByStatus(status, pageable);

        return invoices.map(this::mapToResponse);
    }

    public Page<InvoiceResponse> getInvoicesByPaymentMode(com.hireconnect.subscription.entity.PaymentMode paymentMode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invoice> invoices = invoiceRepository.findByPaymentMode(paymentMode, pageable);

        return invoices.map(this::mapToResponse);
    }

    public long getPendingInvoicesCount(Long subscriptionId) {
        return invoiceRepository.countBySubscriptionIdAndStatus(subscriptionId, InvoiceStatus.PENDING);
    }

    public Double getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.totalRevenueByPeriod(InvoiceStatus.PAID, startDate, endDate);
    }

    public long getTotalInvoicesCount(InvoiceStatus status) {
        return invoiceRepository.countByStatus(status);
    }

    public Page<InvoiceResponse> getAllInvoices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invoice> invoices = invoiceRepository.findAll(pageable);
        
        return invoices.map(this::mapToResponse);
    }

    @Transactional
    public void processOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(LocalDateTime.now(), InvoiceStatus.PENDING);

        for (Invoice invoice : overdueInvoices) {
            invoice.setStatus(InvoiceStatus.FAILED);
            // Send overdue notification
            sendInvoiceEvent("invoice.overdue", invoice);
        }

        invoiceRepository.saveAll(overdueInvoices);
        log.info("Processed {} overdue invoices", overdueInvoices.size());
    }

    private void sendInvoiceEvent(String eventType, Invoice invoice) {
        try {
            rabbitTemplate.convertAndSend("invoice.exchange", "invoice." + eventType, invoice);
            log.info("Sent {} event for invoice {}", eventType, invoice.getId());
        } catch (Exception e) {
            log.error("Failed to send {} event for invoice {}", eventType, invoice.getId(), e);
        }
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getSubscriptionId(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getPaymentMode(),
                invoice.getTransactionId(),
                invoice.getCreatedAt(),
                invoice.getPaidAt(),
                invoice.getStatus(),
                invoice.getDueDate(),
                invoice.getDescription(),
                invoice.getBillingAddress(),
                invoice.getPaymentMethod(),
                invoice.getCardLastFour(),
                invoice.getReceiptUrl(),
                invoice.getRefundReason(),
                invoice.getRefundedAt(),
                invoice.getRefundedAmount()
        );
    }
}
