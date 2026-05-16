package com.hireconnect.notification.service;

import com.hireconnect.notification.dto.NotificationRequest;
import com.hireconnect.notification.dto.NotificationResponse;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.entity.NotificationType;
import com.hireconnect.notification.exception.NotificationNotFoundException;
import com.hireconnect.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;
    private NotificationRequest notificationRequest;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(1L)
                .userId(101L)
                .title("New Job Alert")
                .message("A new job matches your skills")
                .type(NotificationType.NEW_JOB_POSTED)
                .isRead(false)
                .build();

        notificationRequest = new NotificationRequest();
        notificationRequest.setUserId(101L);
        notificationRequest.setTitle("New Job Alert");
        notificationRequest.setMessage("A new job matches your skills");
        notificationRequest.setType(NotificationType.NEW_JOB_POSTED);
    }

    @Test
    void createNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.createNotification(notificationRequest);

        assertNotNull(response);
        assertEquals("New Job Alert", response.getTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L, 101L);

        assertTrue(notification.getIsRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void markAsRead_Unauthorized_ThrowsException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(IllegalArgumentException.class, () -> notificationService.markAsRead(1L, 999L));
    }

    @Test
    void getNotificationById_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.getNotificationById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getNotificationById_NotFound_ThrowsException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> notificationService.getNotificationById(1L));
    }
}
