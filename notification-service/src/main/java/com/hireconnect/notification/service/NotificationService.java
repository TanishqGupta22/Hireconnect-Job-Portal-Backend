package com.hireconnect.notification.service;

import com.hireconnect.notification.dto.NotificationRequest;
import com.hireconnect.notification.dto.NotificationResponse;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.entity.NotificationType;
import com.hireconnect.notification.exception.NotificationNotFoundException;
import com.hireconnect.notification.repository.NotificationRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .actionUrl(request.getActionUrl())
                .actionText(request.getActionText())
                .metadata(request.getMetadata())
                .emailSent(false)
                .priority(request.getPriority())
                .expiresAt(request.getExpiresAt())
                .isArchived(false)
                .build();

        notification = notificationRepository.save(notification);

        // Send email notification for high priority notifications
        if ("HIGH".equals(request.getPriority())) {
            sendEmailNotification(notification, request.getRecipientEmail());
        }

        // Send real-time notification event
        sendNotificationEvent("notification.created", notification);

        log.info("Created notification for user {}: {}", request.getUserId(), request.getTitle());

        return mapToResponse(notification);
    }

    @Transactional
    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only mark your own notifications as read");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        notificationRepository.save(notification);

        // Send notification event
        sendNotificationEvent("notification.read", notification);

        log.info("Marked notification {} as read for user {}", id, userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(unreadNotifications);

        log.info("Marked all notifications as read for user {}", userId);
    }

    @Transactional
    public void archiveNotification(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only archive your own notifications");
        }

        notification.setIsArchived(true);
        notification.setArchivedAt(LocalDateTime.now());

        notificationRepository.save(notification);

        log.info("Archived notification {} for user {}", id, userId);
    }

    @Transactional
    public void archiveAllNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsArchivedFalse(userId);
        
        for (Notification notification : notifications) {
            notification.setIsArchived(true);
            notification.setArchivedAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(notifications);

        log.info("Archived all notifications for user {}", userId);
    }

    @Transactional
    public void deleteNotification(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own notifications");
        }

        notificationRepository.delete(notification);

        log.info("Deleted notification {} for user {}", id, userId);
    }

    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        return mapToResponse(notification);
    }

    public Page<NotificationResponse> getAllNotifications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByUserIdAndIsArchivedFalse(userId, pageable);

        return notifications.map(this::mapToResponse);
    }

    public Page<NotificationResponse> getUnreadNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseAndIsArchivedFalse(userId, pageable);

        return notifications.map(this::mapToResponse);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadAndUnarchivedNotificationsByUserId(userId);
    }

    public List<NotificationResponse> getNotificationsByType(Long userId, NotificationType type) {
        List<Notification> notifications = notificationRepository.findByUserIdAndType(userId, type);
        return notifications.stream()
                .filter(n -> !n.getIsArchived())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cleanupExpiredNotifications() {
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(LocalDateTime.now());
        
        for (Notification notification : expiredNotifications) {
            notification.setIsArchived(true);
            notification.setArchivedAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(expiredNotifications);

        log.info("Archived {} expired notifications", expiredNotifications.size());
    }

    @Transactional
    public void sendBulkNotifications(List<Long> userIds, NotificationRequest request) {
        for (Long userId : userIds) {
            NotificationRequest userRequest = new NotificationRequest();
            userRequest.setUserId(userId);
            userRequest.setTitle(request.getTitle());
            userRequest.setMessage(request.getMessage());
            userRequest.setType(request.getType());
            userRequest.setActionUrl(request.getActionUrl());
            userRequest.setActionText(request.getActionText());
            userRequest.setMetadata(request.getMetadata());
            userRequest.setPriority(request.getPriority());
            userRequest.setExpiresAt(request.getExpiresAt());

            createNotification(userRequest);
        }

        log.info("Sent bulk notifications to {} users", userIds.size());
    }

    private void sendEmailNotification(Notification notification, String recipientEmail) {
        try {
            emailService.sendNotificationEmail(
                    notification.getUserId(),
                    recipientEmail,
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getActionUrl()
            );
            
            notification.setEmailSent(true);
            notification.setEmailSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            log.info("Sent email notification for notification ID: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email notification for notification ID: {}", notification.getId(), e);
        }
    }

    private void sendNotificationEvent(String eventType, Notification notification) {
        try {
            rabbitTemplate.convertAndSend("notification.exchange", "notification." + eventType, notification);
            log.info("Sent {} event for notification {}", eventType, notification.getId());
        } catch (Exception e) {
            log.error("Failed to send {} event for notification {}", eventType, notification.getId(), e);
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getActionUrl(),
                notification.getActionText(),
                notification.getMetadata(),
                notification.getEmailSent(),
                notification.getEmailSentAt(),
                notification.getPriority(),
                notification.getExpiresAt(),
                notification.getIsArchived(),
                notification.getArchivedAt()
        );
    }
}
