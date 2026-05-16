package com.hireconnect.application.dto;

import com.hireconnect.application.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private Long recruiterId;
    private String jobTitle;
    private String companyName;
    private String candidateName;
    private String candidateEmail;
    private String resumeUrl;
    private String coverLetter;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private String notes;
    private String recruiterNotes;
    private Integer rating;
    private Boolean isShortlisted;
    private Boolean isArchived;
    private String rejectionReason;
    private LocalDateTime interviewScheduledAt;
    private String interviewMode;
    private String interviewLocation;
    private String interviewLink;
}
