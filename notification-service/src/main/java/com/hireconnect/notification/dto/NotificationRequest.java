package com.hireconnect.notification.dto;

import com.hireconnect.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;
    
    @NotNull(message = "Notification type is required")
    private NotificationType type;
    
    @Size(max = 500, message = "Action URL must not exceed 500 characters")
    private String actionUrl;
    
    @Size(max = 100, message = "Action text must not exceed 100 characters")
    private String actionText;
    
    private String metadata;
    
    private String priority = "MEDIUM";
    
    private LocalDateTime expiresAt;
    
    private String recipientEmail;
}
