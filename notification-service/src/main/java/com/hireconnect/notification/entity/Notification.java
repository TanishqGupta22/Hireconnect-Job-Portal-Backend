package com.hireconnect.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private NotificationType type;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime readAt;

    @Column(nullable = true)
    private String actionUrl;

    @Column(nullable = true)
    private String actionText;

    @Column(nullable = true)
    private String metadata; // JSON string for additional data

    @Column(nullable = true)
    private Boolean emailSent = false;

    @Column(nullable = true)
    private LocalDateTime emailSentAt;

    @Column(nullable = true)
    private String priority; // LOW, MEDIUM, HIGH

    @Column(nullable = true)
    private LocalDateTime expiresAt;

    @Column(nullable = true)
    private Boolean isArchived = false;

    @Column(nullable = true)
    private LocalDateTime archivedAt;
}
