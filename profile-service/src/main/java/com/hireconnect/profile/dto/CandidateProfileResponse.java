package com.hireconnect.profile.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.io.Serializable;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String fullName;
    private String headline;
    private String mobile;
    private List<String> skills;
    private List<String> experience;
    private List<String> education;
    private String resumeUrl;
    private String profileImageUrl;
    private String address;
    private String linkedin;
    private String github;
    private String portfolio;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublic;
    private Boolean isAvailable;
    private Boolean jobAlertsEnabled;
}
