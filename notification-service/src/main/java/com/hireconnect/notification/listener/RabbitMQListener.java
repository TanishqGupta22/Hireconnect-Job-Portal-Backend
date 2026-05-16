package com.hireconnect.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.notification.dto.NotificationRequest;
import java.util.Map;
import com.hireconnect.notification.entity.NotificationType;
import com.hireconnect.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQListener {

    private final NotificationService notificationService;
    private final com.hireconnect.notification.client.ProfileClient profileClient;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "application.created.queue")
    public void handleApplicationCreated(Map<String, Object> application) {
        try {
            Long id = application.get("id") != null ? ((Number) application.get("id")).longValue() : null;
            Long recruiterId = application.get("recruiterId") != null ? ((Number) application.get("recruiterId")).longValue() : null;
            Long jobId = application.get("jobId") != null ? ((Number) application.get("jobId")).longValue() : null;
            
            String candidateName = application.get("candidateName") != null ? application.get("candidateName").toString() : "A candidate";
            String jobTitle = application.get("jobTitle") != null ? application.get("jobTitle").toString() : "your job posting";
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(recruiterId);
            notification.setTitle("New Application: " + jobTitle);
            notification.setMessage(String.format("%s has applied for the position of %s.", candidateName, jobTitle));
            notification.setType(NotificationType.APPLICATION_RECEIVED);
            notification.setActionUrl("/recruiter/applicants?jobId=" + jobId);
            notification.setActionText("View Application");
            notification.setPriority("HIGH");

            notificationService.createNotification(notification);
            log.info("Processed application created event for application ID: {}", id);
        } catch (Exception e) {
            log.error("Error processing application created event", e);
        }
    }

    @RabbitListener(queues = "application.status.changed.queue")
    public void handleApplicationStatusChanged(Map<String, Object> application) {
        try {
            Long id = application.get("id") != null ? ((Number) application.get("id")).longValue() : null;
            Long candidateId = application.get("candidateId") != null ? ((Number) application.get("candidateId")).longValue() : null;
            String status = application.get("status") != null ? application.get("status").toString() : "UNKNOWN";
            
            String title = "Application Status Updated";
            String message;
            
            if ("SHORTLISTED".equalsIgnoreCase(status)) {
                title = "Application Shortlisted!";
                message = String.format("Congratulations! Your application for %s has been shortlisted.", application.get("jobTitle"));
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                title = "Application Update";
                message = String.format("We regret to inform you that your application for %s was not selected at this time.", application.get("jobTitle"));
            } else {
                message = String.format("Your application status for %s has been updated to: %s", application.get("jobTitle"), status);
            }
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(candidateId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(NotificationType.APPLICATION_STATUS_CHANGED);
            notification.setActionUrl("/applications/" + id);
            notification.setActionText("View Application");
            if ("SHORTLISTED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {
                notification.setPriority("HIGH");
            } else {
                notification.setPriority("MEDIUM");
            }

            notificationService.createNotification(notification);
            log.info("Processed application status changed event for application ID: {}", id);
        } catch (Exception e) {
            log.error("Error processing application status changed event", e);
        }
    }

    @RabbitListener(queues = "interview.scheduled.queue")
    public void handleInterviewScheduled(Map<String, Object> interview) {
        try {
            Long id = interview.get("id") != null ? ((Number) interview.get("id")).longValue() : null;
            Long candidateId = interview.get("candidateId") != null ? ((Number) interview.get("candidateId")).longValue() : null;
            Object scheduledAt = interview.get("scheduledAt");
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(candidateId);
            notification.setTitle("Interview Scheduled");
            notification.setMessage("You have an interview scheduled on " + scheduledAt);
            notification.setType(NotificationType.INTERVIEW_SCHEDULED);
            notification.setActionUrl("/interviews/" + id);
            notification.setActionText("View Interview Details");
            notification.setPriority("HIGH");

            notificationService.createNotification(notification);
            log.info("Processed interview scheduled event for interview ID: {}", id);
        } catch (Exception e) {
            log.error("Error processing interview scheduled event", e);
        }
    }

    @RabbitListener(queues = "interview.confirmed.queue")
    public void handleInterviewConfirmed(Map<String, Object> interview) {
        try {
            Long id = interview.get("id") != null ? ((Number) interview.get("id")).longValue() : null;
            Long recruiterId = interview.get("recruiterId") != null ? ((Number) interview.get("recruiterId")).longValue() : null;
            Object scheduledAt = interview.get("scheduledAt");
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(recruiterId);
            notification.setTitle("Interview Confirmed");
            notification.setMessage("Candidate has confirmed the interview scheduled on " + scheduledAt);
            notification.setType(NotificationType.INTERVIEW_CONFIRMED);
            notification.setActionUrl("/interviews/" + id);
            notification.setActionText("View Interview Details");
            notification.setPriority("MEDIUM");

            notificationService.createNotification(notification);
            log.info("Processed interview confirmed event for interview ID: {}", id);
        } catch (Exception e) {
            log.error("Error processing interview confirmed event", e);
        }
    }

    @RabbitListener(queues = "interview.rescheduled.queue")
    public void handleInterviewRescheduled(Map<String, Object> interview) {
        try {
            Long id = interview.get("id") != null ? ((Number) interview.get("id")).longValue() : null;
            Long candidateId = interview.get("candidateId") != null ? ((Number) interview.get("candidateId")).longValue() : null;
            Object scheduledAt = interview.get("scheduledAt");
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(candidateId);
            notification.setTitle("Interview Rescheduled");
            notification.setMessage("Your interview has been rescheduled to " + scheduledAt);
            notification.setType(NotificationType.INTERVIEW_RESCHEDULED);
            notification.setActionUrl("/interviews/" + id);
            notification.setActionText("View New Schedule");
            notification.setPriority("HIGH");

            notificationService.createNotification(notification);
            log.info("Processed interview rescheduled event for interview ID: {}", id);
        } catch (Exception e) {
            log.error("Error processing interview rescheduled event", e);
        }
    }

    @RabbitListener(queues = "interview.cancelled.queue")
    public void handleInterviewCancelled(Map<String, Object> interview) {
        try {
            Long id = interview.get("id") != null ? ((Number) interview.get("id")).longValue() : null;
            Long candidateId = interview.get("candidateId") != null ? ((Number) interview.get("candidateId")).longValue() : null;
            Long applicationId = interview.get("applicationId") != null ? ((Number) interview.get("applicationId")).longValue() : null;
            Object scheduledAt = interview.get("scheduledAt");
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(candidateId);
            notification.setTitle("Interview Cancelled");
            notification.setMessage("Your interview scheduled for " + scheduledAt + " has been cancelled");
            notification.setType(NotificationType.INTERVIEW_CANCELLED);
            notification.setActionUrl("/applications/" + applicationId);
            notification.setActionText("View Application");
            notification.setPriority("HIGH");

            notificationService.createNotification(notification);
            log.info("Processed interview cancelled event for interview ID: {}", id);
        } catch (Exception e) {
            log.error("Error processing interview cancelled event", e);
        }
    }

    @RabbitListener(queues = "profile.viewed.queue")
    public void handleProfileViewed(Map<String, Object> event) {
        try {
            Long candidateId = event.get("candidateId") != null ? ((Number) event.get("candidateId")).longValue() : null;
            String viewerName = event.get("viewerName") != null ? event.get("viewerName").toString() : "Someone";
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(candidateId);
            notification.setTitle("Profile Viewed");
            notification.setMessage(viewerName + " has viewed your profile.");
            notification.setType(NotificationType.PROFILE_VIEWED);
            notification.setActionUrl("/profile");
            notification.setActionText("View My Profile");
            notification.setPriority("LOW");

            notificationService.createNotification(notification);
            log.info("Processed profile viewed event for candidate ID: {}", candidateId);
        } catch (Exception e) {
            log.error("Error processing profile viewed event", e);
        }
    }

    @RabbitListener(queues = "job.created.queue")
    public void handleJobCreated(Map<String, Object> job) {
        try {
            Long id = job.get("id") != null ? ((Number) job.get("id")).longValue() : null;
            String title = job.get("title") != null ? job.get("title").toString() : "New Job";
            java.util.List<String> jobSkills = job.get("skills") != null ? (java.util.List<String>) job.get("skills") : new java.util.ArrayList<>();
            
            log.info("Processing job created event for job: {}. Skills: {}", title, jobSkills);
            
            // Get all candidates with alerts enabled
            java.util.List<com.hireconnect.notification.client.ProfileClient.CandidateProfileResponse> candidates = profileClient.getAvailableCandidates();
            
            for (com.hireconnect.notification.client.ProfileClient.CandidateProfileResponse candidate : candidates) {
                if (Boolean.TRUE.equals(candidate.getJobAlertsEnabled())) {
                    // Simple skill matching: if any job skill is in candidate skills
                    boolean matches = false;
                    if (jobSkills.isEmpty()) {
                        matches = true; // Notify all if no specific skills listed? Or maybe not.
                    } else {
                        for (String skill : jobSkills) {
                            if (candidate.getSkills() != null && candidate.getSkills().stream().anyMatch(s -> s.equalsIgnoreCase(skill))) {
                                matches = true;
                                break;
                            }
                        }
                    }
                    
                    if (matches) {
                        NotificationRequest notification = new NotificationRequest();
                        notification.setUserId(candidate.getUserId());
                        notification.setTitle("New Job Alert: " + title);
                        notification.setMessage("A new job matching your skills has been posted: " + title);
                        notification.setType(NotificationType.NEW_JOB_POSTED);
                        notification.setActionUrl("/job-details/" + id);
                        notification.setActionText("View Job");
                        notification.setPriority("MEDIUM");

                        notificationService.createNotification(notification);
                        log.info("Sent job alert for job {} to user {}", id, candidate.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing job created event", e);
        }
    }

    @RabbitListener(queues = "user.registered.queue")
    public void handleUserRegistered(Map<String, Object> user) {
        System.out.println("Received registration event");
        System.out.println(user);
        
        System.out.println("SENDING EMAIL TO: " + (user.get("email") != null ? user.get("email") : "UNKNOWN"));
        
        try {
            Long userId = user.get("userId") != null ? ((Number) user.get("userId")).longValue() : null;
            String name = user.get("name") != null ? user.get("name").toString() : "User";
            String email = user.get("email") != null ? user.get("email").toString() : "";
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(userId);
            notification.setTitle("Welcome to HireConnect!");
            notification.setMessage(String.format("Hi %s, thank you for joining HireConnect. We're excited to help you find your dream job!", name));
            notification.setType(NotificationType.USER_REGISTERED);
            notification.setActionUrl("/profile");
            notification.setActionText("Complete Your Profile");
            notification.setRecipientEmail(email); // Explicitly set recipient email
            notification.setPriority("HIGH"); // High priority sends email

            notificationService.createNotification(notification);
            log.info("Processed user registered event for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error processing user registered event", e);
        }
    }

    @RabbitListener(queues = "user.password.reset.queue")
    public void handlePasswordReset(Map<String, Object> event) {
        try {
            Long userId = event.get("id") != null ? ((Number) event.get("id")).longValue() : null;
            String resetToken = event.get("resetToken") != null ? event.get("resetToken").toString() : "";
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(userId);
            notification.setTitle("Password Reset Request");
            notification.setMessage("A password reset was requested for your account. Use the link below to set a new password.");
            notification.setType(NotificationType.PASSWORD_RESET);
            notification.setActionUrl("/auth/reset-password?token=" + resetToken);
            notification.setActionText("Reset Password");
            notification.setPriority("HIGH"); // High priority sends email

            notificationService.createNotification(notification);
            log.info("Processed password reset event for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Error processing password reset event", e);
        }
    }

    @RabbitListener(queues = "admin.alert.queue")
    public void handleAdminAlert(Map<String, Object> alert) {
        try {
            String title = alert.get("title") != null ? alert.get("title").toString() : "System Alert";
            String message = alert.get("message") != null ? alert.get("message").toString() : "An important system event occurred.";
            
            // Get all admin users (simplified: using ID 1 for now)
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(1L); // Default admin ID
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(NotificationType.SECURITY_ALERT);
            notification.setPriority("HIGH");

            notificationService.createNotification(notification);
            log.info("Processed admin alert: {}", title);
        } catch (Exception e) {
            log.error("Error processing admin alert", e);
        }
    }
}
