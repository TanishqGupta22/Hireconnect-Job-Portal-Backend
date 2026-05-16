package com.hireconnect.application.dto;

import com.hireconnect.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private ApplicationStatus status;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @Size(max = 1000, message = "Recruiter notes must not exceed 1000 characters")
    private String recruiterNotes;
    
    @NotNull(message = "Reviewed by is required")
    private Long reviewedBy;
    
    private Integer rating; // 1-5 rating
    
    private String rejectionReason;
    
    private String interviewMode;
    
    private String interviewLocation;
    
    private String interviewLink;
}
