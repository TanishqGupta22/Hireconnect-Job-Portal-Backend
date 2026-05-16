package com.hireconnect.profile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.io.Serializable;

@Entity
@Table(name = "candidate_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = true)
    private String headline;

    @Column(nullable = false)
    private String mobile;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill")
    private List<String> skills;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "candidate_experience", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "experience")
    private List<String> experience;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "candidate_education", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "education")
    private List<String> education;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String resumeUrl;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = true)
    private String linkedin;

    @Column(nullable = true)
    private String github;

    @Column(nullable = true)
    private String portfolio;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(nullable = true)
    private Boolean isPublic = true;

    @Builder.Default
    @Column(nullable = true)
    private Boolean isAvailable = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean jobAlertsEnabled = false;
}
