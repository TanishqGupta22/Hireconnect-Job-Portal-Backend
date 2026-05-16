package com.hireconnect.job.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recruiter_id", nullable = false)
    private Long recruiterId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> skills;

    @Column(name = "category", nullable = true)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType type;

    @Column(name = "location", nullable = true)
    private String location;

    @Column(name = "work_mode", nullable = true)
    private String workMode; // REMOTE, HYBRID, ONSITE

    @Column(name = "salary_min", nullable = true)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", nullable = true)
    private BigDecimal salaryMax;

    @Builder.Default
    @Column(name = "salary_currency", nullable = true)
    private String salaryCurrency = "USD";

    @Column(name = "experience_required", nullable = true)
    private Integer experienceRequired;

    @Column(name = "education_level", nullable = true)
    private String educationLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "expires_at", nullable = true)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(name = "is_featured", nullable = true)
    private Boolean isFeatured = false;

    @Builder.Default
    @Column(name = "is_remote", nullable = true)
    private Boolean isRemote = false;

    @Column(name = "application_deadline", nullable = true)
    private String applicationDeadline;

    @Builder.Default
    @Column(name = "vacancy_count", nullable = true)
    private Integer vacancyCount = 1;

    @Column(name = "department", nullable = true)
    private String department;

    @Column(name = "benefits", nullable = true, columnDefinition = "LONGTEXT")
    private String benefits;

    @Column(name = "requirements", nullable = true, columnDefinition = "LONGTEXT")
    private String requirements;

    @Column(name = "responsibilities", nullable = true, columnDefinition = "LONGTEXT")
    private String responsibilities;
}
