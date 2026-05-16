package com.hireconnect.notification.controller;

import com.hireconnect.notification.dto.NotificationRequest;
import com.hireconnect.notification.dto.NotificationResponse;
import com.hireconnect.notification.entity.NotificationType;
import com.hireconnect.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.getAllNotifications(page, size));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createNotification(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all/{userId}")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Void> archiveNotification(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.archiveNotification(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/archive-all/{userId}")
    public ResponseEntity<Void> archiveAllNotifications(@PathVariable Long userId) {
        notificationService.archiveAllNotifications(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, page, size));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId, page, size));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByType(
            @PathVariable Long userId,
            @PathVariable NotificationType type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(userId, type));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> sendBulkNotifications(@RequestParam List<Long> userIds, @Valid @RequestBody NotificationRequest request) {
        notificationService.sendBulkNotifications(userIds, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupExpiredNotifications() {
        notificationService.cleanupExpiredNotifications();
        return ResponseEntity.ok().build();
    }
}
