package com.hireconnect.notification.repository;

import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    Page<Notification> findByUserIdAndIsReadFalse(Long userId, Pageable pageable);
    
    Page<Notification> findByUserIdAndIsArchivedFalse(Long userId, Pageable pageable);
    
    Page<Notification> findByUserIdAndIsReadFalseAndIsArchivedFalse(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    
    List<Notification> findByUserIdAndIsArchivedFalse(Long userId);
    
    List<Notification> findByType(NotificationType type);
    
    List<Notification> findByUserIdAndType(Long userId, NotificationType type);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :startDate AND n.createdAt <= :endDate")
    List<Notification> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadNotificationsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isArchived = false")
    long countUnreadAndUnarchivedNotificationsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.expiresAt < :now AND n.isArchived = false")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Notification n WHERE n.emailSent = false AND n.priority = 'HIGH'")
    List<Notification> findHighPriorityUnsentEmailNotifications();
    
    void deleteByUserId(Long userId);
    
    void deleteByUserIdAndIsArchivedTrue(Long userId);
    
    @Query("DELETE FROM Notification n WHERE n.expiresAt < :now")
    void deleteExpiredNotifications(@Param("now") LocalDateTime now);
}
