package com.hireconnect.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplicationRequest {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    @NotNull(message = "Candidate ID is required")
    private Long candidateId;
    
    @Size(max = 500, message = "Resume URL must not exceed 500 characters")
    private String resumeUrl;
    
    @Size(max = 2000, message = "Cover letter must not exceed 2000 characters")
    private String coverLetter;

    private String jobTitle;
    private String companyName;
    private Long recruiterId;
    private String candidateName;
    private String candidateEmail;
}
