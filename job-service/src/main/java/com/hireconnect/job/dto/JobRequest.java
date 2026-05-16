package com.hireconnect.job.dto;

import com.hireconnect.job.entity.JobType;
import com.hireconnect.job.entity.JobStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobRequest {
    
    @NotNull(message = "Recruiter ID is required")
    private Long recruiterId;
    
    @NotBlank(message = "Job title is required")
    @Size(min = 5, max = 200, message = "Job title must be between 5 and 200 characters")
    private String title;
    
    @NotBlank(message = "Job description is required")
    @Size(min = 50, max = 5000, message = "Job description must be between 50 and 5000 characters")
    private String description;
    
    private List<String> skills;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    @NotNull(message = "Job type is required")
    private JobType type;
    
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;
    
    @Size(max = 50, message = "Work mode must not exceed 50 characters")
    private String workMode;
    
    @DecimalMin(value = "0.0", message = "Minimum salary must be positive")
    private BigDecimal salaryMin;
    
    @DecimalMin(value = "0.0", message = "Maximum salary must be positive")
    private BigDecimal salaryMax;
    
    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String salaryCurrency = "USD";
    
    @Min(value = 0, message = "Experience required must be non-negative")
    @Max(value = 50, message = "Experience required must not exceed 50 years")
    private Integer experienceRequired;
    
    @Size(max = 100, message = "Education level must not exceed 100 characters")
    private String educationLevel;
    
    private LocalDateTime expiresAt;
    
    private Boolean isFeatured = false;
    
    private Boolean isRemote = false;
    
    private String applicationDeadline;
    
    @Min(value = 1, message = "Vacancy count must be at least 1")
    private Integer vacancyCount = 1;
    
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    @Size(max = 1000, message = "Benefits must not exceed 1000 characters")
    private String benefits;
    
    @Size(max = 2000, message = "Requirements must not exceed 2000 characters")
    private String requirements;
    
    @Size(max = 2000, message = "Responsibilities must not exceed 2000 characters")
    private String responsibilities;
}
