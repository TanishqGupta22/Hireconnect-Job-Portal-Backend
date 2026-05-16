package com.hireconnect.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "recruiter_id", nullable = true)
    private Long recruiterId;
    @Column(name = "job_title", nullable = true)
    private String jobTitle;
    @Column(name = "company_name", nullable = true)
    private String companyName;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "candidate_name", nullable = true)
    private String candidateName;

    @Column(name = "candidate_email", nullable = true)
    private String candidateEmail;

    @Column(name = "resume_url", nullable = true)
    private String resumeUrl;

    @Column(name = "cover_letter", nullable = true, columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    @Column(name = "reviewed_at", nullable = true)
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by", nullable = true)
    private Long reviewedBy;

    @Column(name = "notes", nullable = true, columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recruiter_notes", nullable = true)
    private String recruiterNotes;

    @Column(name = "rating", nullable = true)
    private Integer rating; // 1-5 rating by recruiter

    @Builder.Default
    @Column(name = "is_shortlisted", nullable = true)
    private Boolean isShortlisted = false;

    @Builder.Default
    @Column(name = "is_archived", nullable = true)
    private Boolean isArchived = false;

    @Column(name = "rejection_reason", nullable = true)
    private String rejectionReason;

    @Column(name = "interview_scheduled_at", nullable = true)
    private LocalDateTime interviewScheduledAt;

    @Column(name = "interview_mode", nullable = true)
    private String interviewMode;

    @Column(name = "interview_location", nullable = true)
    private String interviewLocation;

    @Column(name = "interview_link", nullable = true)
    private String interviewLink;
}
