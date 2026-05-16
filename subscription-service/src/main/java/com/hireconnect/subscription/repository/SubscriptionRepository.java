package com.hireconnect.subscription.repository;

import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.entity.SubscriptionPlan;
import com.hireconnect.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    Optional<Subscription> findByUserId(Long userId);
    
    Optional<Subscription> findByUserIdAndUserRole(Long userId, String userRole);
    
    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);
    
    Page<Subscription> findByPlan(SubscriptionPlan plan, Pageable pageable);
    
    List<Subscription> findByUserIdAndUserRoleAndStatus(Long userId, String userRole, SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :now AND s.status = :status")
    List<Subscription> findExpiredSubscriptions(@Param("now") LocalDateTime now, @Param("status") SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingAt <= :now AND s.autoRenew = true AND s.status = :status")
    List<Subscription> findSubscriptionsForRenewal(@Param("now") LocalDateTime now, @Param("status") SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.trialEndsAt <= :now AND s.isTrial = true AND s.status = :status")
    List<Subscription> findExpiringTrials(@Param("now") LocalDateTime now, @Param("status") SubscriptionStatus status);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.plan = :plan AND s.status = :status")
    long countByPlanAndStatus(@Param("plan") SubscriptionPlan plan, @Param("status") SubscriptionStatus status);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    long countByStatus(@Param("status") SubscriptionStatus status);
    
    @Query("SELECT SUM(s.amount) FROM Subscription s WHERE s.status = :status AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    Double totalRevenueByPeriod(@Param("status") SubscriptionStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    boolean existsByUserIdAndUserRole(Long userId, String userRole);
    
    void deleteByUserIdAndUserRole(Long userId, String userRole);
}
