package com.hireconnect.subscription.repository;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.InvoiceStatus;
import com.hireconnect.subscription.entity.PaymentMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Page<Invoice> findBySubscriptionId(Long subscriptionId, Pageable pageable);
    
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    
    Page<Invoice> findByPaymentMode(PaymentMode paymentMode, Pageable pageable);
    
    List<Invoice> findBySubscriptionIdAndStatus(Long subscriptionId, InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :now AND i.status = :status")
    List<Invoice> findOverdueInvoices(@Param("now") LocalDateTime now, @Param("status") InvoiceStatus status);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.subscriptionId = :subscriptionId AND i.status = :status")
    long countBySubscriptionIdAndStatus(@Param("subscriptionId") Long subscriptionId, @Param("status") InvoiceStatus status);
    
    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.status = :status AND i.paidAt >= :startDate AND i.paidAt <= :endDate")
    Double totalRevenueByPeriod(@Param("status") InvoiceStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.createdAt >= :startDate AND i.createdAt <= :endDate")
    List<Invoice> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    List<Invoice> findByTransactionId(String transactionId);
}
