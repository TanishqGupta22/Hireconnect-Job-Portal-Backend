package com.hireconnect.job.dto;

import com.hireconnect.job.entity.JobStatus;
import com.hireconnect.job.entity.JobType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class JobResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long recruiterId;
    private String title;
    private String description;
    private List<String> skills;
    private String category;
    private JobType type;
    private String location;
    private String workMode;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private Integer experienceRequired;
    private String educationLevel;
    private JobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private Boolean isFeatured;
    private Boolean isRemote;
    private String applicationDeadline;
    private Integer vacancyCount;
    private String department;
    private String benefits;
    private String requirements;
    private String responsibilities;
    private Long applicantCount;
}
