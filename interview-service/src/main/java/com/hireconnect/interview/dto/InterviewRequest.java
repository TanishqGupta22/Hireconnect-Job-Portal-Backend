package com.hireconnect.interview.dto;

import com.hireconnect.interview.entity.InterviewMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewRequest {
    
    @NotNull(message = "Application ID is required")
    private Long applicationId;
    
    @NotNull(message = "Recruiter ID is required")
    private Long recruiterId;
    
    @NotNull(message = "Candidate ID is required")
    private Long candidateId;
    
    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    
    @NotNull(message = "Interview mode is required")
    private InterviewMode mode;
    
    @Size(max = 500, message = "Meeting link must not exceed 500 characters")
    private String meetingLink;
    
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;
    
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
    
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 480, message = "Duration must not exceed 480 minutes")
    private Integer duration;
    
    @Size(max = 100, message = "Interview type must not exceed 100 characters")
    private String interviewType;
    
    @Size(max = 100, message = "Interviewer name must not exceed 100 characters")
    private String interviewerName;

    private String candidateName;
    
    private String jobTitle;
}
