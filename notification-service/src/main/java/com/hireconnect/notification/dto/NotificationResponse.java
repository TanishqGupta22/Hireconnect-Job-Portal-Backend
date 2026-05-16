package com.hireconnect.notification.dto;

import com.hireconnect.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String actionUrl;
    private String actionText;
    private String metadata;
    private Boolean emailSent;
    private LocalDateTime emailSentAt;
    private String priority;
    private LocalDateTime expiresAt;
    private Boolean isArchived;
    private LocalDateTime archivedAt;
}
