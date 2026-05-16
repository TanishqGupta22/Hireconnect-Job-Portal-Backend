package com.hireconnect.interview.dto;

import com.hireconnect.interview.entity.InterviewMode;
import com.hireconnect.interview.entity.InterviewStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InterviewResponse {
    private Long id;
    private Long applicationId;
    private Long recruiterId;
    private Long candidateId;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    private InterviewMode mode;
    private String meetingLink;
    private String location;
    private String notes;
    private InterviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime rescheduledAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Integer duration;
    private String interviewType;
    private String interviewerName;
    private String feedback;
    private Integer rating;
    private String outcome;
    private String candidateName;
    private String jobTitle;
}
