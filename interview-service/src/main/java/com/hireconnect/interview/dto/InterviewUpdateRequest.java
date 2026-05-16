package com.hireconnect.interview.dto;

import com.hireconnect.interview.entity.InterviewMode;
import com.hireconnect.interview.entity.InterviewStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewUpdateRequest {
    
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledAt;
    
    private InterviewMode mode;
    
    @Size(max = 500, message = "Meeting link must not exceed 500 characters")
    private String meetingLink;
    
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;
    
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
    
    private InterviewStatus status;
    
    @Size(max = 1000, message = "Cancellation reason must not exceed 1000 characters")
    private String cancellationReason;
    
    @Size(max = 2000, message = "Feedback must not exceed 2000 characters")
    private String feedback;
    
    private Integer rating; // 1-5 rating
    
    @Size(max = 50, message = "Outcome must not exceed 50 characters")
    private String outcome;
}
