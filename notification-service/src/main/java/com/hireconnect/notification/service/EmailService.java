package com.hireconnect.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendNotificationEmail(Long userId, String recipientEmail, String title, String message, String actionUrl) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(recipientEmail != null && !recipientEmail.isEmpty() ? recipientEmail : "user@example.com"); // This should be fetched from user service
            email.setSubject("HireConnect - " + title);
            email.setText(buildEmailContent(title, message, actionUrl));

            mailSender.send(email);
            log.info("Sent notification email to user {}: {}", userId, title);
        } catch (Exception e) {
            log.error("Failed to send notification email to user {}: {}", userId, title, e);
        }
    }

    private String buildEmailContent(String title, String message, String actionUrl) {
        StringBuilder content = new StringBuilder();
        content.append("Hi there!\n\n");
        content.append("You have a new notification from HireConnect:\n\n");
        content.append("Title: ").append(title).append("\n\n");
        content.append("Message: ").append(message).append("\n\n");
        
        if (actionUrl != null && !actionUrl.trim().isEmpty()) {
            content.append("Click here to view more details: ").append(actionUrl).append("\n\n");
        }
        
        content.append("Thank you for using HireConnect!\n");
        content.append("Best regards,\n");
        content.append("The HireConnect Team");
        
        return content.toString();
    }
}
