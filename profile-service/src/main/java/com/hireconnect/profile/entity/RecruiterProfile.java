package com.hireconnect.profile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

@Entity
@Table(name = "recruiter_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "website", nullable = true)
    private String website;

    @Column(name = "industry", nullable = true)
    private String industry;

    @Column(name = "company_size", nullable = true)
    private String companySize;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "logo_url", columnDefinition = "LONGTEXT")
    private String logoUrl;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(nullable = true)
    private String linkedin;

    @Column(name = "contact_email", nullable = true)
    private String contactEmail;

    @Column(name = "contact_phone", nullable = true)
    private String contactPhone;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_verified", nullable = true)
    private Boolean isVerified = false;

    @Builder.Default
    @Column(name = "is_active", nullable = true)
    private Boolean isActive = true;
}
