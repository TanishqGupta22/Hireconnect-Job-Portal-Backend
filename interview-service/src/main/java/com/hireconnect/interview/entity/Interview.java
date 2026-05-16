package com.hireconnect.interview.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long recruiterId;

    @Column(nullable = false)
    private Long candidateId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewMode mode;

    @Column(nullable = true)
    private String meetingLink;

    @Column(nullable = true)
    private String location;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime confirmedAt;

    @Column(nullable = true)
    private LocalDateTime rescheduledAt;

    @Column(nullable = true)
    private LocalDateTime completedAt;

    @Column(nullable = true)
    private LocalDateTime cancelledAt;

    @Column(nullable = true)
    private String cancellationReason;

    @Column(nullable = true)
    private Integer duration; // in minutes

    @Column(nullable = true)
    private String interviewType; // Technical, HR, Final, etc.

    @Column(nullable = true)
    private String interviewerName;

    @Column(nullable = true)
    private String candidateName;

    @Column(nullable = true)
    private String jobTitle;

    @Column(nullable = true)
    private String feedback;

    @Column(nullable = true)
    private Integer rating; // 1-5 rating

    @Column(nullable = true)
    private String outcome; // PASS, FAIL, HOLD
}
